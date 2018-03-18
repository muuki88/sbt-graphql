object BlogFragmentsApi {
  case class BlogFragments(blog: BlogFragmentsApi.BlogFragments.Blog)
  object BlogFragments {
    case class BlogFragmentsVariables(blogId: BlogFragmentsApi.ID, pagination: Pagination)
    case class Blog(title: String, articles: List[BlogFragmentsApi.BlogFragments.Blog.Articles], articlesWithAuthorId: List[BlogFragmentsApi.BlogFragments.Blog.ArticlesWithAuthorId])
    object Blog {
      case class Articles(title: String, author: BlogFragmentsApi.BlogFragments.Blog.Articles.Author)
      object Articles { case class Author(id: BlogFragmentsApi.ID, name: String) }
      case class ArticlesWithAuthorId(id: BlogFragmentsApi.ID, title: String, author: BlogFragmentsApi.BlogFragments.Blog.ArticlesWithAuthorId.Author)
      object ArticlesWithAuthorId { case class Author(id: BlogFragmentsApi.ID, name: String) }
    }
  }
  case class Pagination(first: Int, count: Int, order: Option[PaginationOrder])
  sealed trait PaginationOrder
  object PaginationOrder {
    case object ASC extends BlogFragmentsApi.PaginationOrder
    case object DESC extends BlogFragmentsApi.PaginationOrder
  }
  type ID = String
}
