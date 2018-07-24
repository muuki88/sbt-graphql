import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sangria.macros._
object SearchQuery {
  object SearchQuery extends GraphQLQuery {
    type Document = sangria.ast.Document
    val document: Document = graphql"""query SearchQuery($$text: String!) {
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
    case class Data(search: List[Search])
    sealed trait Search {
      def __typename: String
      def name: Option[String]
    }
    object Search {
      case class Human(__typename: String, name: Option[String], secretBackstory: Option[String]) extends Search
      object Human { implicit val jsonDecoder: Decoder[Human] = deriveDecoder[Human] }
      case class Droid(__typename: String, name: Option[String], primaryFunction: Option[String]) extends Search
      object Droid { implicit val jsonDecoder: Decoder[Droid] = deriveDecoder[Droid] }
      case class Starship(__typename: String, name: Option[String]) extends Search
      object Starship { implicit val jsonDecoder: Decoder[Starship] = deriveDecoder[Starship] }
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
    }
  }
}
