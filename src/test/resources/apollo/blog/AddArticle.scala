import sangria.macros._
import types._
object AddArticle {
  object addArticle extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""mutation addArticle($$content: ArticleContent!) {
  addArticle(content: $$content) {
    id
    title
  }
}"""
    case class Variables(content: ArticleContent)
    case class Data(addArticle: AddArticle)
    case class AddArticle(id: ID, title: String)
  }
}
