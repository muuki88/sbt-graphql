import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros._
import types._
object AllAnimals {
  object AllAnimals extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query AllAnimals {
  animals {
    ...AnimalName
  }
}

fragment AnimalName on Animal {
  ...DogName
  ...CatName
}
fragment DogName on Dog {
  name
  barks
}
fragment CatName on Cat {
  name
  lifes
}"""
    case class Variables()
    object Variables { implicit val jsonEncoder: Encoder[Variables] = deriveEncoder[Variables] }
    case class Data(animals: List[Animals])
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    sealed trait Animals { def name: String }
    object Animals {
      case class Cat(name: String, lifes: Int) extends Animals
      object Cat {
        implicit val jsonDecoder: Decoder[Cat] = deriveDecoder[Cat]
        implicit val jsonEncoder: Encoder[Cat] = deriveEncoder[Cat]
      }
      case class Dog(name: String, barks: Boolean) extends Animals
      object Dog {
        implicit val jsonDecoder: Decoder[Dog] = deriveDecoder[Dog]
        implicit val jsonEncoder: Encoder[Dog] = deriveEncoder[Dog]
      }
    }
  }
}