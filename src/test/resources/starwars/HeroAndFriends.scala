object HeroAndFriendsApi {
  case class HeroAndFriends(hero: HeroAndFriendsApi.HeroAndFriends.Hero)
  object HeroAndFriends {
    case class HeroAndFriendsVariables()
    case class Hero(name: Option[String], friends: Option[List[Option[HeroAndFriendsApi.HeroAndFriends.Hero.Friends]]])
    object Hero {
      case class Friends(name: Option[String], friends: Option[List[Option[HeroAndFriendsApi.HeroAndFriends.Hero.Friends.Friends]]])
      object Friends {
        case class Friends(name: Option[String], friends: Option[List[Option[HeroAndFriendsApi.HeroAndFriends.Hero.Friends.Friends.Friends]]])
        object Friends {
          case class Friends(name: Option[String], friends: Option[List[Option[HeroAndFriendsApi.HeroAndFriends.Hero.Friends.Friends.Friends.Friends]]])
          object Friends { case class Friends(name: Option[String]) }
        }
      }
    }
  }
}
