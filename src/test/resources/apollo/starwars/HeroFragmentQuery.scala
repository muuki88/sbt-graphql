import sangria.macros._
object HeroFragmentQuery {
  object HeroFragmentQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroFragmentQuery {
  hero {
    ...CharacterInfo
  }
  human(id: "Lea") {
    ...CharacterInfo
  }
}"""
    case class Variables()
    case class Data(hero: Hero, human: Option[Human])
    case class Hero(name: Option[String]) extends CharacterInfo
    case class Human(name: Option[String]) extends CharacterInfo
  }
  trait CharacterInfo { def name: Option[String] }
}
