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
    case class Data(search: List[Search], searchWithFragmentSpread: List[SearchResultFragment], searchOnImplements: List[SearchResultOnIdentifiableFragment])
  }
}