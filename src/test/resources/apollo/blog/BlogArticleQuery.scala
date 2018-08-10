import sangria.macros._
import types._
object BlogArticleQuery {
  object BlogArticleQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query BlogArticleQuery($$query: ArticleQuery!) {
  articles(query: $$query) {
    id
    status
  }
}"""
    case class Variables(query: ArticleQuery)
    case class Data(articles: List[Articles])
    case class Articles(id: ID, status: ArticleStatus)
  }
}
