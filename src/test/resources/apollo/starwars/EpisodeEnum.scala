import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sangria.macros._
object EpisodeEnum {
  object EpisodeEnum extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query EpisodeEnum {
  hero {
    name
    appearsIn
  }
}"""
    case class Variables()
    case class Data(hero: Hero)
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Hero(name: Option[String], appearsIn: Option[List[Option[Episode]]])
    object Hero { implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero] }
  }
  sealed trait Episode
  object Episode {
    case object NEWHOPE extends Episode
    case object EMPIRE extends Episode
    case object JEDI extends Episode
  }
}
