import com.example.GraphQLQuery
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sangria.macros._
object HeroNameQuery {
  object HeroNameQuery extends GraphQLQuery {
    val Document = graphql"""query HeroNameQuery {
  hero {
    name
  }
}"""
    case class Variables()
    case class Data(hero: Hero)
    object Data { implicit val jsonDecoder: Decoder[Data] = deriveDecoder[Data] }
    case class Hero(name: Option[String])
    object Hero { implicit val jsonDecoder: Decoder[Hero] = deriveDecoder[Hero] }
  }
}
