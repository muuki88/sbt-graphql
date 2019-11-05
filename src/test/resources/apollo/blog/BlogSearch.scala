import sangria.macros._
import types._
object BlogSearch {
  object BlogSearch extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query BlogSearch($$text: String!, $$pagination: Pagination!) {
  search(text: $$text, pagination: $$pagination) {
    ... on Identifiable {
      ...IdFragment
    }
    ... on Blog {
      ...BlogFragment
    }
    ... on Article {
      ...ArticleFragment
    }
    ... on Author {
      ...AuthorFragment
    }
  }
  searchWithFragmentSpread: search(text: $$text, pagination: $$pagination) {
    ...SearchResultFragment
  }
  searchOnImplements: search(text: $$text, pagination: $$pagination) {
    ...SearchResultOnIdentifiableFragment
  }
}

fragment IdFragment on Identifiable {
  id
}
fragment ArticleFragment on Article {
  __typename
  title
  status
  author {
    ...AuthorFragment
  }
}
fragment BlogFragment on Blog {
  __typename
  title
}
fragment SearchResultOnIdentifiableFragment on Identifiable {
  id
  ... on Blog {
    ...BlogFragment
  }
  ... on Article {
    __typename
    title
    status
    author {
      name
    }
  }
  ... on Author {
    __typename
    name
  }
}
fragment AuthorFragment on Author {
  __typename
  name
}
fragment SearchResultFragment on SearchResult {
  ... on Identifiable {
    ...IdFragment
  }
  ... on Blog {
    ...BlogFragment
  }
  ... on Article {
    ...ArticleFragment
  }
  ... on Author {
    ...AuthorFragment
  }
}"""
    case class Variables(text: String, pagination: Pagination)
    case class Data(search: List[Search], searchWithFragmentSpread: List[SearchWithFragmentSpread], searchOnImplements: List[SearchOnImplements])
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
    sealed trait SearchWithFragmentSpread {
      def __typename: String
      def id: ID
    }
    object SearchWithFragmentSpread {
      case class Blog(id: ID, __typename: String, title: String) extends SearchWithFragmentSpread
      case class Article(id: ID, __typename: String, title: String, status: ArticleStatus, author: SearchWithFragmentSpread.Author) extends SearchWithFragmentSpread
      object Article { case class Author(__typename: String, name: String) extends AuthorFragment }
      case class Author(id: ID, __typename: String, name: String) extends SearchWithFragmentSpread
    }
    sealed trait SearchOnImplements {
      def __typename: String
      def id: ID
    }
    object SearchOnImplements {
      case class Blog(id: ID, __typename: String, title: String) extends SearchOnImplements
      case class Article(id: ID, __typename: String, title: String, status: ArticleStatus, author: SearchOnImplements.Author) extends SearchOnImplements
      object Article { case class Author(name: String) }
      case class Author(id: ID, __typename: String, name: String) extends SearchOnImplements
    }
  }
}