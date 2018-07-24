import com.example.GraphQLQuery
import sangria.macros._
object HeroNameQuery {
  object HeroNameQuery extends GraphQLQuery {
    type Document = sangria.ast.Document
    val document: Document = graphql"""query HeroNameQuery {
  hero {
    name
  }
}"""
    case class Variables()
    case class Data(hero: Hero)
    case class Hero(name: Option[String])
  }
}
