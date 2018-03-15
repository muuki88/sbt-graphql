object BlogArticleQueryApi {
  case class BlogArticleQuery(articles: List[BlogArticleQueryApi.BlogArticleQuery.Articles])
  object BlogArticleQuery {
    case class BlogArticleQueryVariables(query: ArticleQuery)
    case class Articles(id: BlogArticleQueryApi.ID, status: ArticleStatus)
  }
  case class ArticleQuery(ids: Option[List[BlogArticleQueryApi.ID]], statuses: Option[List[ArticleStatus]])
  sealed trait ArticleStatus
  object ArticleStatus {
    case object DRAFT extends BlogArticleQueryApi.ArticleStatus
    case object PUBLISHED extends BlogArticleQueryApi.ArticleStatus
  }
  type ID = String
}
