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
    case class Data(hero: Hero, human: Option[Human])
    case class Hero(name: Option[String]) extends CharacterInfo
    case class Human(name: Option[String]) extends CharacterInfo
  }
}
