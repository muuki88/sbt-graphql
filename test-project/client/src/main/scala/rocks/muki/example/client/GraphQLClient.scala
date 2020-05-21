package rocks.muki.example.client

import cats.data._
import cats.syntax.all._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import rocks.muki.graphql.GraphQLQuery
import sangria.ast.Document

/**
  * This is an example of a simple GraphQL Scala client.
  *
  * We use something very similar in production (but it uses akka-http instead of requests-scala and is therefore more
  * involved).
  */
object GraphQLClient {

  /**
    * We piggy-back on `cats.data.IorNel` as it models very nicely how graphql works: Even if there are errors we might
    * still get data back to render at least some of the frontend. Only when we get no data at all (= `Ior.Left`)
    * we have to display a generic 500 page or something like that.
    */
  type GraphQLResult[Data] = IorNel[GraphQLError, Data]

  /**
    * Sends a GraphQL query or mutation to the graphql-service and returns a type-safe GraphQLResult.
    *
    * The function has multiple argument lists as the type of `variables` depends on `query` (= path-dependent-types)
    * and Scala is not yet able to solve resolve this otherwise (might be fixed at some point).
    *
    * If there's an unexpected error (e.g. connection issues) the result will be a `Ior.Left` containing
    * a `GraphQLError`. This makes it easier to write error-handling once and makes the page more
    * reliable/usable.
    *
    * @param query A GraphQL query or mutation defined inline or parsed from a file (both via sangria)
    * @param variables Input data needed by the query. Usually a case class or Map
    */
  def sendQuery(
      graphqlServerUrl: String,
      query: GraphQLQuery
  )(
      variables: Option[query.Variables]
  )(implicit
      encode: Encoder[query.Variables],
      decode: Decoder[query.Data]
  ): GraphQLResult[query.Data] =
    sendQueryRaw[GraphQLResponse[query.Data]](graphqlServerUrl, query)(variables)
      .toIor
      // transforming exceptions into GraphQLErrors makes your downstream code simpler as you only have to handle one
      // type of error (instead of GraphQLError + Exceptions).
      .leftMap(exc => NonEmptyList.one(GraphQLError.fromThrowable(exc)))
      .flatMap(_.asResult)

  /**
    * Send a GraphQL query like `sendQuery` but do not parse the result.
    *
    * Use this method instead of `sendQuery` when you need to get hold of the raw result, eg, to forward it to the frontend.
    */
  def sendQueryRaw[Response](
      graphqlServerUrl: String,
      query: GraphQLQuery
  )(
      variables: Option[query.Variables]
  )(implicit
      enc: Encoder[query.Variables],
      dec: Decoder[Response]
  ): Either[Throwable, Response] = Either.catchNonFatal { // A bit lazy but it works ;)
    // Encode query + variables as json. You technically don't have to use json but 99.9% of web-devs want to.
    val payload = GraphQLPayload[query.Variables](query.document, variables).asJson.noSpaces
    pprint.log(payload)

    // Send the request to the graphql-server
    val response = requests.post(graphqlServerUrl, data = payload, headers = List("content-type" -> "application/json"))

    // Decode the response. We fail fast here, as the code generation should make sure this always works.
    val decodedResponse = parser.decode[Response](response.text()).valueOr(throw _)

    decodedResponse
  }

  /**
    * Combines A query/mutation and its input variables into one serializable payload.
    *
    * @tparam Vars Type of the input variables. Requires a circe Encoder instance
    */
  final case class GraphQLPayload[Vars](operationName: String, query: Document, variables: Option[Vars])
  object GraphQLPayload {
    def apply[Vars](query: Document, variables: Option[Vars]): GraphQLPayload[Vars] =
      new GraphQLPayload(query.operations.values.flatMap(_.name).mkString(","), query, variables)

    implicit private val graphqlDocumentEncoder: Encoder[Document] =
      Encoder[String].contramap(_.renderCompact)

    implicit def queryEncoder[Vars](implicit queryEncoder: Encoder[Vars]): Encoder[GraphQLPayload[Vars]] =
      deriveEncoder[GraphQLPayload[Vars]]
  }

  /**
    * Represents the parsed json response from the server before it is parsed into the [[GraphQLResult]] ADT.
    *
    * @tparam Data Type of the expected response data json. Usually represented by a case class.
    */
  final case class GraphQLResponse[Data](data: Option[Data], errors: Option[NonEmptyList[GraphQLError]]) {

    def asResult: GraphQLResult[Data] = (data, errors) match {
      case (Some(data), None) =>
        Ior.right(data)
      case (Some(data), Some(errors)) =>
        Ior.both(errors, data)
      case (None, Some(errors)) =>
        Ior.left(errors)
      case (None, None) =>
        Ior.leftNel(GraphQLError(s"GraphQL response contained neither 'data' nor 'errors'!", path = List.empty))
    }
  }
  object GraphQLResponse {
    implicit def responseDecoder[Data](implicit dec: Decoder[Data]): Decoder[GraphQLResponse[Data]] = deriveDecoder
  }

  /**
    * Represents a structured graphql error. Use `reason` and `path` to differentiate errors
    *
    * @param message An error message or description of the error.
    * @param path The json path to the field this error belongs to (which is then `null`).
    */
  final case class GraphQLError(message: String, path: List[String])
  object GraphQLError {

    def fromThrowable(exc: Throwable): GraphQLError = GraphQLError(exc.getMessage, path = List.empty)

    /**
      * The path may include numbers (index of lists), so it is actually `List[Any]`, but we can transform numbers
      * to String as well to keep our sanity.
      */
    implicit private val pathDecoder: Decoder[List[String]] =
      Decoder.decodeList(Decoder.decodeString or Decoder.decodeInt.map(_.toString))

    implicit val errorDecoder: Decoder[GraphQLError] = deriveDecoder
  }
}
