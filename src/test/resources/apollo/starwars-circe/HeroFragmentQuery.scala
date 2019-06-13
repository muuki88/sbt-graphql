import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros._
import types._
object HeroFragmentQuery {
  object HeroFragmentQuery extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query HeroFragmentQuery {
  hero {
    ...CharacterInfo
  }
  human(id: "Lea") {
    ...CharacterInfo
  }
}

fragment CharacterInfo on Character {
  name
}"""
    case class Variables()
    object Variables { implicit val jsonEncoder: Encoder[Variables] = deriveEncoder[Variables] }
    case class Data(hero: Hero, human: Option[Human])
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Hero(name: Option[String]) extends CharacterInfo
    object Hero {
      implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero]
      implicit val jsonEncoder: Encoder[Hero] = deriveEncoder[Hero]
    }
    case class Human(name: Option[String]) extends CharacterInfo
    object Human {
      implicit val jsonDecoder: Decoder[Human] = deriveDecoder[Human]
      implicit val jsonEncoder: Encoder[Human] = deriveEncoder[Human]
    }
  }
}