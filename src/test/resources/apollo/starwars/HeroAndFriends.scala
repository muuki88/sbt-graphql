import sangria.macros._
object HeroAndFriends {
  object HeroAndFriends extends GraphQLQuery {
    type Document = sangria.ast.Document
    val document: Document = graphql"""query HeroAndFriends {
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
    case class Hero(name: Option[String], friends: Option[List[Option[Hero.Friends]]])
    object Hero {
      case class Friends(name: Option[String], friends: Option[List[Option[Friends.Friends]]])
      object Friends {
        case class Friends(name: Option[String], friends: Option[List[Option[Friends.Friends]]])
        object Friends {
          case class Friends(name: Option[String], friends: Option[List[Option[Friends.Friends]]])
          object Friends { case class Friends(name: Option[String]) }
        }
      }
    }
  }
}
