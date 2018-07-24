import com.example.GraphQLQuery
import sangria.macros._
object AddArticle {
  object addArticle extends GraphQLQuery {
    type Document = sangria.ast.Document
    val document: Document = graphql"""mutation addArticle($$content: ArticleContent!) {
  addArticle(content: $$content) {
    id
    title
  }
}"""
    case class Variables(content: ArticleContent)
    case class Data(addArticle: AddArticle)
    case class AddArticle(id: ID, title: String)
  }
  case class ArticleAuthor(id: ID)
  case class ArticleContent(title: String, body: String, tags: Option[List[String]], author: ArticleAuthor)
  type ID = String
}
