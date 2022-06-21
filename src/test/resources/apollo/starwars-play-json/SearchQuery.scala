import play.api.libs.json.{ Json, Reads, Writes, JsValue, JsObject, JsString, JsSuccess, JsError }
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
    object Variables { implicit val jsonWrites: Writes[Variables] = Json.writes[Variables] }
    case class Data(search: List[Search])
    object Data { implicit val jsonReads: Reads[Data] = Json.reads[Data] }
    sealed trait Search {
      def __typename: String
      def name: Option[String]
    }
    object Search {
      case class Human(__typename: String, name: Option[String], secretBackstory: Option[String]) extends Search
      object Human {
        implicit val jsonReads: Reads[Human] = Json.reads[Human]
        implicit val jsonWrites: Writes[Human] = Json.writes[Human]
      }
      case class Droid(__typename: String, name: Option[String], primaryFunction: Option[String]) extends Search
      object Droid {
        implicit val jsonReads: Reads[Droid] = Json.reads[Droid]
        implicit val jsonWrites: Writes[Droid] = Json.writes[Droid]
      }
      case class Starship(__typename: String, name: Option[String]) extends Search
      object Starship {
        implicit val jsonReads: Reads[Starship] = Json.reads[Starship]
        implicit val jsonWrites: Writes[Starship] = Json.writes[Starship]
      }
      implicit val jsonReads: Reads[Search] = (json: JsValue) => for (valueAsObject <- json.validate[JsObject]; typeDiscriminator <- (valueAsObject \ "__typename").validate[String]; value <- typeDiscriminator match {
        case "Human" =>
          valueAsObject.validate[Human]
        case "Droid" =>
          valueAsObject.validate[Droid]
        case "Starship" =>
          valueAsObject.validate[Starship]
        case other =>
          JsError("invalid type: " + other)
      }) yield value
      implicit val jsonWrites: Writes[Search] = {
        case v: Human =>
          Json.toJson(v)
        case v: Droid =>
          Json.toJson(v)
        case v: Starship =>
          Json.toJson(v)
      }
    }
  }
}