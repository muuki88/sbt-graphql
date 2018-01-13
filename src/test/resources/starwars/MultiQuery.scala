object MultiQueryApi {
  case class HeroAndFriends(hero: MultiQueryApi.HeroAndFriends.Hero)
  object HeroAndFriends {
    case class HeroAndFriendsVariables()
    case class Hero(name: Option[String], friends: Option[List[Option[MultiQueryApi.HeroAndFriends.Hero.Friends]]])
    object Hero { case class Friends(name: Option[String]) }
  }
  case class HeroAndNestedFriends(hero: MultiQueryApi.HeroAndNestedFriends.Hero)
  object HeroAndNestedFriends {
    case class HeroAndNestedFriendsVariables()
    case class Hero(name: Option[String], friends: Option[List[Option[MultiQueryApi.HeroAndNestedFriends.Hero.Friends]]])
    object Hero {
      case class Friends(name: Option[String], friends: Option[List[Option[MultiQueryApi.HeroAndNestedFriends.Hero.Friends.Friends]]])
      object Friends {
        case class Friends(name: Option[String], friends: Option[List[Option[MultiQueryApi.HeroAndNestedFriends.Hero.Friends.Friends.Friends]]])
        object Friends {
          case class Friends(name: Option[String], friends: Option[List[Option[MultiQueryApi.HeroAndNestedFriends.Hero.Friends.Friends.Friends.Friends]]])
          object Friends { case class Friends(name: Option[String]) }
        }
      }
    }
  }
  case class FragmentExample(human: Option[MultiQueryApi.FragmentExample.Human], droid: MultiQueryApi.FragmentExample.Droid)
  object FragmentExample {
    case class FragmentExampleVariables()
    case class Human(name: Option[String], appearsIn: Option[List[Option[Episode]]], homePlanet: Option[String])
    case class Droid(name: Option[String], appearsIn: Option[List[Option[Episode]]], primaryFunction: Option[String])
  }
  case class VariableExample(human: Option[MultiQueryApi.VariableExample.Human])
  object VariableExample {
    case class VariableExampleVariables(humanId: String)
    case class Human(name: Option[String], homePlanet: Option[String], friends: Option[List[Option[MultiQueryApi.VariableExample.Human.Friends]]])
    object Human { case class Friends(name: Option[String]) }
  }
  sealed trait Episode
  object Episode {
    case object NEWHOPE extends MultiQueryApi.Episode
    case object EMPIRE extends MultiQueryApi.Episode
    case object JEDI extends MultiQueryApi.Episode
  }
}
