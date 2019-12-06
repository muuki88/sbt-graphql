import types._
object fragments {
  sealed trait Search {
    def __typename: String
    def id: ID
  }
  object Search {
    case class Blog(id: ID, __typename: String, title: String) extends Search
    case class Article(id: ID, __typename: String, title: String, status: ArticleStatus, author: Search.Author) extends Search
    object Article { case class Author(__typename: String, name: String) extends AuthorFragment }
    case class Author(id: ID, __typename: String, name: String) extends Search
  }
  case class IdFragment(id: ID)
  case class ArticleFragment(__typename: String, title: String, status: ArticleStatus, author: AuthorFragment)
  case class BlogFragment(__typename: String, title: String)
  case class SearchResultOnIdentifiableFragment(id: ID, __typename: String, title: String, status: ArticleStatus, author: SearchResultOnIdentifiableFragment.Author, name: String)
  object SearchResultOnIdentifiableFragment { case class Author(name: String) }
  case class AuthorFragment(__typename: String, name: String)
  sealed trait SearchResultFragment
  object SearchResultFragment {
    case class Blog(id: ID, __typename: String, title: String) extends SearchResultFragment
    case class Article(id: ID, __typename: String, title: String, status: ArticleStatus, author: AuthorFragment) extends SearchResultFragment
    case class Author(id: ID, __typename: String, name: String) extends SearchResultFragment
  }
}