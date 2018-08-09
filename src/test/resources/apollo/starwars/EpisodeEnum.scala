import sangria.macros._
import types._
object EpisodeEnum {
  object EpisodeEnum extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query EpisodeEnum {
  hero {
    name
    appearsIn
  }
}"""
    case class Variables()
    case class Data(hero: Hero)
    case class Hero(name: Option[String], appearsIn: Option[List[Option[Episode]]])
  }
}
