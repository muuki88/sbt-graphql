import sangria.macros._
object SearchQuery {
  object SearchQuery extends GraphQLQuery {
    val Document = graphql"""query SearchQuery($$text: String!) {
  search(text: $$text) {
    __typename
    ... on Human {
      name
      secretBackstory
    }
    ... on Droid {
      name
      primaryFunction
    }
    ... on Starship {
      name
    }
  }
}"""
    case class Variables(text: String)
    case class Data(search: List[Search])
    sealed trait Search
    object Search {
      case class Human(__typename: String, name: Option[String], secretBackstory: Option[String]) extends Search
      case class Droid(__typename: String, name: Option[String], primaryFunction: Option[String]) extends Search
      case class Starship(__typename: String, name: Option[String]) extends Search
    }
  }
}