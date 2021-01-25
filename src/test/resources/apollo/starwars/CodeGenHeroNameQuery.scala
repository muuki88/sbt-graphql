import sangria.macros._
import types._
object CodeGenHeroNameQuery {
  object CodeGenHeroNameQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query CodeGenHeroNameQuery {
  hero {
    name
  }
}"""
    case class Variables()
    case class Data(hero: Hero)
  }
}
