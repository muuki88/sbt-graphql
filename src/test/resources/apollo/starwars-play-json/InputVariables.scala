import play.api.libs.json.{ Json, Reads, Writes, JsValue, JsObject, JsString, JsSuccess, JsError }
import sangria.macros._
import types._
object InputVariables {
  object InputVariables extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query InputVariables($$humanId: String!) {
  human(id: $$humanId) {
    name
    homePlanet
  }
}"""
    case class Variables(humanId: String)
    object Variables { implicit val jsonWrites: Writes[Variables] = Json.writes[Variables] }
    case class Data(human: Option[Human])
    object Data { implicit val jsonReads: Reads[Data] = Json.reads[Data] }
    case class Human(name: Option[String], homePlanet: Option[String])
    object Human {
      implicit val jsonReads: Reads[Human] = Json.reads[Human]
      implicit val jsonWrites: Writes[Human] = Json.writes[Human]
    }
  }
}