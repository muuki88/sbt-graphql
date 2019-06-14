import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros._
import types._
object InputVariables {
  object InputVariables extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query InputVariables($$humanId: String!) {
  human(id: $$humanId) {
    name
    homePlanet
  }
}"""
    case class Variables(humanId: String)
    object Variables { implicit val jsonEncoder: Encoder[Variables] = deriveEncoder[Variables] }
    case class Data(human: Option[Human])
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Human(name: Option[String], homePlanet: Option[String])
    object Human {
      implicit val jsonDecoder: Decoder[Human] = deriveDecoder[Human]
      implicit val jsonEncoder: Encoder[Human] = deriveEncoder[Human]
    }
  }
}
