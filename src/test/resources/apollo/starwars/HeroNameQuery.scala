import sangria.macros._
object HeroNameQuery {
  object HeroNameQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroNameQuery {
  hero {
    name
  }
}"""
    case class Variables()
    case class Data(hero: Hero)
    case class Hero(name: Option[String])
  }
}
