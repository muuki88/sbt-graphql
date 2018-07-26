import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sangria.macros._
object HeroAndFriends {
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
    case class Data(hero: Hero)
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Hero(name: Option[String], friends: Option[List[Option[Hero.Friends]]])
    object Hero {
      implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero]
      case class Friends(name: Option[String], friends: Option[List[Option[Friends.Friends]]])
      object Friends {
        implicit val jsonDecoder: Decoder[Friends] = deriveDecoder[Friends]
        case class Friends(name: Option[String], friends: Option[List[Option[Friends.Friends]]])
        object Friends {
          implicit val jsonDecoder: Decoder[Friends] = deriveDecoder[Friends]
          case class Friends(name: Option[String], friends: Option[List[Option[Friends.Friends]]])
          object Friends {
            implicit val jsonDecoder: Decoder[Friends] = deriveDecoder[Friends]
            case class Friends(name: Option[String])
            object Friends { implicit val jsonDecoder: Decoder[Friends] = deriveDecoder[Friends] }
          }
        }
      }
    }
  }
}
