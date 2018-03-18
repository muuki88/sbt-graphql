object BlogSearchApi {
  case class BlogSearch(search: List[BlogSearchApi.BlogSearch.Search], searchWithFragmentSpread: List[BlogSearchApi.BlogSearch.SearchWithFragmentSpread], searchOnImplements: List[BlogSearchApi.BlogSearch.SearchOnImplements])
  object BlogSearch {
    case class BlogSearchVariables(text: String, pagination: Pagination)
    sealed trait Search
    object Search {
      case class Blog(id: BlogSearchApi.ID, __typename: String, title: String) extends BlogSearchApi.BlogSearch.Search
      case class Article(id: BlogSearchApi.ID, __typename: String, title: String, status: ArticleStatus, author: BlogSearchApi.BlogSearch.Search.Article.Author) extends BlogSearchApi.BlogSearch.Search
      object Article { case class Author(__typename: String, name: String) }
      case class Author(id: BlogSearchApi.ID, __typename: String, name: String) extends BlogSearchApi.BlogSearch.Search
    }
    sealed trait SearchWithFragmentSpread
    object SearchWithFragmentSpread {
      case class Blog(id: BlogSearchApi.ID, __typename: String, title: String) extends BlogSearchApi.BlogSearch.SearchWithFragmentSpread
      case class Article(id: BlogSearchApi.ID, __typename: String, title: String, status: ArticleStatus, author: BlogSearchApi.BlogSearch.SearchWithFragmentSpread.Article.Author) extends BlogSearchApi.BlogSearch.SearchWithFragmentSpread
      object Article { case class Author(__typename: String, name: String) }
      case class Author(id: BlogSearchApi.ID, __typename: String, name: String) extends BlogSearchApi.BlogSearch.SearchWithFragmentSpread
    }
    sealed trait SearchOnImplements
    object SearchOnImplements {
      case class Blog(id: BlogSearchApi.ID, __typename: String, title: String) extends BlogSearchApi.BlogSearch.SearchOnImplements
      case class Article(id: BlogSearchApi.ID, __typename: String, title: String, status: ArticleStatus, author: BlogSearchApi.BlogSearch.SearchOnImplements.Article.Author) extends BlogSearchApi.BlogSearch.SearchOnImplements
      object Article { case class Author(name: String) }
      case class Author(id: BlogSearchApi.ID, __typename: String, name: String) extends BlogSearchApi.BlogSearch.SearchOnImplements
    }
  }
  sealed trait ArticleStatus
  object ArticleStatus {
    case object DRAFT extends BlogSearchApi.ArticleStatus
    case object PUBLISHED extends BlogSearchApi.ArticleStatus
  }
  case class Pagination(first: Int, count: Int, order: Option[PaginationOrder])
  sealed trait PaginationOrder
  object PaginationOrder {
    case object ASC extends BlogSearchApi.PaginationOrder
    case object DESC extends BlogSearchApi.PaginationOrder
  }
  type ID = String
}
