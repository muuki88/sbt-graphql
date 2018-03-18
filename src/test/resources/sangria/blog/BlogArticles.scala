object BlogArticlesApi {
  case class BlogArticles(blog: BlogArticlesApi.BlogArticles.Blog)
  object BlogArticles {
    case class BlogArticlesVariables(blogId: BlogArticlesApi.ID, pagination: Pagination)
    case class Blog(title: String, articles: List[BlogArticlesApi.BlogArticles.Blog.Articles])
    object Blog {
      case class Articles(title: String, body: String, tags: List[String], status: ArticleStatus, author: BlogArticlesApi.BlogArticles.Blog.Articles.Author)
      object Articles { case class Author(name: String) }
    }
  }
  sealed trait ArticleStatus
  object ArticleStatus {
    case object DRAFT extends BlogArticlesApi.ArticleStatus
    case object PUBLISHED extends BlogArticlesApi.ArticleStatus
  }
  case class Pagination(first: Int, count: Int, order: Option[PaginationOrder])
  sealed trait PaginationOrder
  object PaginationOrder {
    case object ASC extends BlogArticlesApi.PaginationOrder
    case object DESC extends BlogArticlesApi.PaginationOrder
  }
  type ID = String
}
