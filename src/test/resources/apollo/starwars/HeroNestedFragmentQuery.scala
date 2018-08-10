import sangria.macros._
import types._
object HeroNestedFragmentQuery {
  object HeroNestedFragmentQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroNestedFragmentQuery {
  hero {
    ...CharacterInfo
  }
  human(id: "Lea") {
    ...CharacterInfo
  }
}

fragment CharacterFriends on Character {
  name
}
fragment CharacterInfo on Character {
  name
  friends {
    ...CharacterFriends
  }
}"""
    case class Variables()
    case class Data(hero: Hero, human: Option[Human])
    case class Hero(name: Option[String], friends: Option[List[Option[Hero.Friends]]]) extends CharacterInfo
    object Hero { case class Friends(name: Option[String]) extends CharacterFriends }
    case class Human(name: Option[String], friends: Option[List[Option[Human.Friends]]]) extends CharacterInfo
    object Human { case class Friends(name: Option[String]) extends CharacterFriends }
  }
}