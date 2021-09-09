import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros._
import types._
object SearchQuery {
  object SearchQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query SearchQuery($$text: String!) {
  search(text: $$text) {
    __typename
    ... on Human {
      name
      secretBackstory
    }
    ... on Droid {
      name
      primaryFunction
    }
    ... on Starship {
      name
    }
  }
}"""
    case class Variables(text: String)
    object Variables { implicit val jsonEncoder: Encoder[Variables] = deriveEncoder[Variables] }
    case class Data(search: List[Search])
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    sealed trait Search {
      def __typename: String
      def name: Option[String]
    }
    object Search {
      case class Human(__typename: String, name: Option[String], secretBackstory: Option[String]) extends Search
      object Human {
        implicit val jsonDecoder: Decoder[Human] = deriveDecoder[Human]
        implicit val jsonEncoder: Encoder[Human] = deriveEncoder[Human]
      }
      case class Droid(__typename: String, name: Option[String], primaryFunction: Option[String]) extends Search
      object Droid {
        implicit val jsonDecoder: Decoder[Droid] = deriveDecoder[Droid]
        implicit val jsonEncoder: Encoder[Droid] = deriveEncoder[Droid]
      }
      case class Starship(__typename: String, name: Option[String]) extends Search
      object Starship {
        implicit val jsonDecoder: Decoder[Starship] = deriveDecoder[Starship]
        implicit val jsonEncoder: Encoder[Starship] = deriveEncoder[Starship]
      }
      implicit val jsonDecoder: Decoder[Search] = for (typeDiscriminator <- Decoder[String].prepare(_.downField("__typename")); value <- typeDiscriminator match {
        case "Human" =>
          Decoder[Human]
        case "Droid" =>
          Decoder[Droid]
        case "Starship" =>
          Decoder[Starship]
        case other =>
          Decoder.failedWithMessage("invalid type: " + other)
      }) yield value
      implicit val jsonEncoder: Encoder[Search] = Encoder.instance[Search]({
        case v: Human =>
          deriveEncoder[Human].apply(v)
        case v: Droid =>
          deriveEncoder[Droid].apply(v)
        case v: Starship =>
          deriveEncoder[Starship].apply(v)
      })
    }
  }
}