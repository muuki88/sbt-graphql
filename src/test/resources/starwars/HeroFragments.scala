object HeroFragmentsApi {
  case class HeroAppearancesAndInline(hero: HeroFragmentsApi.HeroAppearancesAndInline.Hero)
  object HeroAppearancesAndInline {
    case class HeroAppearancesAndInlineVariables()
    case class Hero(name: Option[String], appearsIn: Option[List[Option[Episode]]], id: String)
  }
  case class HeroAppearances(hero: HeroFragmentsApi.HeroAppearances.Hero)
  object HeroAppearances {
    case class HeroAppearancesVariables()
    case class Hero(name: Option[String], appearsIn: Option[List[Option[Episode]]])
  }
  case class HeroAppearancesAndInlineAlias(hero: HeroFragmentsApi.HeroAppearancesAndInlineAlias.Hero)
  object HeroAppearancesAndInlineAlias {
    case class HeroAppearancesAndInlineAliasVariables()
    case class Hero(name: Option[String], appearsIn: Option[List[Option[Episode]]], heroId: String)
  }
  case class HeroFragmentOverlap(hero: HeroFragmentsApi.HeroFragmentOverlap.Hero)
  object HeroFragmentOverlap {
    case class HeroFragmentOverlapVariables()
    case class Hero(id: String, name: Option[String], appearsIn: Option[List[Option[Episode]]])
  }
  case class HeroNameAliasAndAppearances(hero: HeroFragmentsApi.HeroNameAliasAndAppearances.Hero)
  object HeroNameAliasAndAppearances {
    case class HeroNameAliasAndAppearancesVariables()
    case class Hero(alias: Option[String], name: Option[String], appearsIn: Option[List[Option[Episode]]])
  }
  sealed trait Episode
  object Episode {
    case object NEWHOPE extends HeroFragmentsApi.Episode
    case object EMPIRE extends HeroFragmentsApi.Episode
    case object JEDI extends HeroFragmentsApi.Episode
  }
}
