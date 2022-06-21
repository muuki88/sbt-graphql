import play.api.libs.json.{ Json, Reads, Writes, JsValue, JsObject, JsString, JsSuccess, JsError }
import sangria.macros._
import types._
object HeroNameQuery {
  object HeroNameQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroNameQuery {
  hero {
    name
  }
}"""
    case class Variables()
    object Variables { implicit val jsonWrites: Writes[Variables] = Json.writes[Variables] }
    case class Data(hero: Hero)
    object Data { implicit val jsonReads: Reads[Data] = Json.reads[Data] }
    case class Hero(name: Option[String])
    object Hero {
      implicit val jsonReads: Reads[Hero] = Json.reads[Hero]
      implicit val jsonWrites: Writes[Hero] = Json.writes[Hero]
    }
  }
}