import com.example.GraphQLQuery
import sangria.macros._
object BlogArticleQuery {
  object BlogArticleQuery extends GraphQLQuery {
    type Document = sangria.ast.Document
    val document: Document = graphql"""query BlogArticleQuery($$query: ArticleQuery!) {
  articles(query: $$query) {
    id
    status
  }
}"""
    case class Variables(query: ArticleQuery)
    case class Data(articles: List[Articles])
    case class Articles(id: ID, status: ArticleStatus)
  }
  case class ArticleQuery(ids: Option[List[ID]], statuses: Option[List[ArticleStatus]])
  sealed trait ArticleStatus
  object ArticleStatus {
    case object DRAFT extends ArticleStatus
    case object PUBLISHED extends ArticleStatus
  }
  type ID = String
}
