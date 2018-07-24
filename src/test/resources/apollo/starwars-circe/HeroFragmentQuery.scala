import com.example.GraphQLQuery
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sangria.macros._
object HeroFragmentQuery {
  object HeroFragmentQuery extends GraphQLQuery {
    type Document = sangria.ast.Document
    val document: Document = graphql"""query HeroFragmentQuery {
  hero {
    ...CharacterInfo
  }
  human(id: "Lea") {
    ...CharacterInfo
  }
}"""
    case class Variables()
    case class Data(hero: Hero, human: Option[Human])
    case class Hero(name: Option[String]) extends CharacterInfo
    object Hero { implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero] }
    case class Human(name: Option[String]) extends CharacterInfo
    object Human { implicit val jsonDecoder: Decoder[Human] = deriveDecoder[Human] }
  }
  trait CharacterInfo { def name: Option[String] }
}
