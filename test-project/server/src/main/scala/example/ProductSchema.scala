package example

import java.time._
import java.time.format.DateTimeFormatter

import sangria._
import sangria.marshalling._
import sangria.validation._
import sangria.schema._
import sangria.macros.derive._

import scala.util._

// From the Sangria getting started guide
// http://sangria-graphql.org/getting-started/

trait Identifiable {
  def id: String
}

case class Picture(width: Int, height: Int, url: Option[String], createdAt: LocalDateTime)
case class Product(id: String, name: String, description: String) extends Identifiable {
  def picture(size: Int): Picture =
    Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"), createdAt = LocalDateTime.now())
}


object LocalDateTimeScalar {

  case object LocalDateTimeCoercionViolation extends ValueCoercionViolation("LocalDateTime value expected")

  private def parseDate(s: String) = Try(LocalDateTime.parse(s)) match {
    case Success(date) => Right(date)
    case Failure(_) => Left(LocalDateTimeCoercionViolation)
  }

  val LocalDateTimeType = ScalarType[LocalDateTime]("LocalDateTime",
    coerceOutput = (localDateTime, caps) =>
      if (caps.contains(DateSupport)) localDateTime.toLocalDate
      else DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime),
    coerceUserInput = {
      case s: String => parseDate(s)
      case _ => Left(LocalDateTimeCoercionViolation)
    },
    coerceInput = {
      case ast.StringValue(s, _, _, _, _) => parseDate(s)
      case _ => Left(LocalDateTimeCoercionViolation)
    })

}


object ProductSchema {

  import LocalDateTimeScalar._


  implicit val PictureType = ObjectType(
    "Picture",
    "The product picture",

    fields[Unit, Picture](
      Field("width", IntType, resolve = _.value.width),
      Field("height", IntType, resolve = _.value.height),
      Field("url", OptionType(StringType),
        description = Some("Picture CDN URL"),
        resolve = _.value.url)))

  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",

    fields[Unit, Identifiable](
      Field("id", StringType, resolve = _.value.id)))

  val ProductType =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture"))


  val Id = Argument("id", StringType)

  val QueryType = ObjectType("Query", fields[ProductRepo, Unit](
    Field("product", OptionType(ProductType),
      description = Some("Returns a product with specific `id`."),
      arguments = Id :: Nil,
      resolve = c => c.ctx.product(c arg Id)),

    Field("products", ListType(ProductType),
      description = Some("Returns a list of all available products."),
      resolve = _.ctx.products)))

  val schema = Schema(
    query = QueryType,
    additionalTypes = List(LocalDateTimeType)
  )

}

class ProductRepo {
  private val Products = List(
    Product("1", "Cheesecake", "Tasty"),
    Product("2", "Health Potion", "+50 HP"))

  def product(id: String): Option[Product] =
    Products find (_.id == id)

  def products: List[Product] = Products
}
