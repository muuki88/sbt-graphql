import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
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
    object Variables { implicit val jsonEncoder: Encoder[Variables] = deriveEncoder[Variables] }
    case class Data(hero: Hero)
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Hero(name: Option[String], friends: Option[List[Option[Hero.Friends]]])
    object Hero {
      implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero]
      implicit val jsonEncoder: Encoder[Hero] = deriveEncoder[Hero]
      case class Friends(name: Option[String], friends: com.example.CustomFriend)
      object Friends {
        implicit val jsonDecoder: Decoder[Friends] = deriveDecoder[Friends]
        implicit val jsonEncoder: Encoder[Friends] = deriveEncoder[Friends]
      }
    }
  }
}