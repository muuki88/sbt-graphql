object HeroNameQueryApi {
  case class HeroNameQuery(hero: HeroNameQueryApi.HeroNameQuery.Hero)
  object HeroNameQuery {
    case class HeroNameQueryVariables()
    case class Hero(name: Option[String])
  }
}
