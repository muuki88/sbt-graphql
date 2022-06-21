import play.api.libs.json.{ Json, Reads, Writes, JsValue, JsObject, JsString, JsSuccess, JsError }
import sangria.macros._
import types._
object HeroFragmentQuery {
  object HeroFragmentQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroFragmentQuery {
  hero {
    ...CharacterInfo
  }
  human(id: "Lea") {
    ...CharacterInfo
  }
}

fragment CharacterInfo on Character {
  name
}"""
    case class Variables()
    object Variables { implicit val jsonWrites: Writes[Variables] = Json.writes[Variables] }
    case class Data(hero: Hero, human: Option[Human])
    object Data { implicit val jsonReads: Reads[Data] = Json.reads[Data] }
    case class Hero(name: Option[String]) extends CharacterInfo
    object Hero {
      implicit val jsonReads: Reads[Hero] = Json.reads[Hero]
      implicit val jsonWrites: Writes[Hero] = Json.writes[Hero]
    }
    case class Human(name: Option[String]) extends CharacterInfo
    object Human {
      implicit val jsonReads: Reads[Human] = Json.reads[Human]
      implicit val jsonWrites: Writes[Human] = Json.writes[Human]
    }
  }
}