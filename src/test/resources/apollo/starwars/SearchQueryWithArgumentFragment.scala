import sangria.macros._
import types._
object SearchQueryWithArgumentFragment {
  object SearchQueryWithArgumentFragment extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query SearchQueryWithArgumentFragment($$text: String!, $$lengthUnit: LengthUnit = METER) {
  search(text: $$text) {
    ...StarshipDetail
    ...HumanDetail
    ...DroidDetail
  }
}

fragment StarshipDetail on Starship {
  name
  length(unit: $$lengthUnit)
}
fragment HumanDetail on Human {
  name
}
fragment DroidDetail on Droid {
  name
}"""
    case class Variables(text: String, lengthUnit: Option[LengthUnit])
    case class Data(search: List[Search])
    sealed trait Search { def name: Option[String] }
    object Search {
      case class Human(name: Option[String]) extends Search
      case class Droid(name: Option[String]) extends Search
      case class Starship(name: Option[String], length: Option[Float]) extends Search
    }
  }
}
