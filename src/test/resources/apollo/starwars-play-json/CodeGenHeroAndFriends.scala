import play.api.libs.json.{ Json, Reads, Writes, JsValue, JsObject, JsString, JsSuccess, JsError }
import sangria.macros._
import types._
object CodeGenHeroAndFriends {
  object HeroAndFriends extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroAndFriends {
  hero {
    name
    friends {
      name
      friends {
        name
        friends {
          name
          friends {
            name
          }
        }
      }
    }
  }
}"""
    case class Variables()
    object Variables { implicit val jsonWrites: Writes[Variables] = Json.writes[Variables] }
    case class Data(hero: Hero)
    object Data { implicit val jsonReads: Reads[Data] = Json.reads[Data] }
    case class Hero(name: Option[String], friends: Option[List[Option[Hero.Friends]]])
    object Hero {
      implicit val jsonReads: Reads[Hero] = Json.reads[Hero]
      implicit val jsonWrites: Writes[Hero] = Json.writes[Hero]
      case class Friends(name: Option[String], friends: com.example.CustomFriend)
      object Friends {
        implicit val jsonReads: Reads[Friends] = Json.reads[Friends]
        implicit val jsonWrites: Writes[Friends] = Json.writes[Friends]
      }
    }
  }
}