import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros._
import types._
object EpisodeEnum {
  object EpisodeEnum extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query EpisodeEnum {
  hero {
    name
    appearsIn
  }
}"""
    case class Variables()
    object Variables { implicit val jsonEncoder: Encoder[Variables] = deriveEncoder[Variables] }
    case class Data(hero: Hero)
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Hero(name: Option[String], appearsIn: Option[List[Option[Episode]]])
    object Hero {
      implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero]
      implicit val jsonEncoder: Encoder[Hero] = deriveEncoder[Hero]
    }
  }
}