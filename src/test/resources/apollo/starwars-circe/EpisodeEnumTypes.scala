import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
object types {
  sealed trait Episode extends Product with Serializable
  object Episode {
    case object NEWHOPE extends Episode
    case object EMPIRE extends Episode
    case object JEDI extends Episode
    implicit val jsonDecoder: Decoder[Episode] = Decoder.decodeString.emap({
      case "NEWHOPE" =>
        Right(NEWHOPE)
      case "EMPIRE" =>
        Right(EMPIRE)
      case "JEDI" =>
        Right(JEDI)
      case other =>
        Left("invalid enum value: " + other)
    })
    implicit val jsonEncoder: Encoder[Episode] = Encoder.encodeString.contramap({
      case NEWHOPE => "NEWHOPE"
      case EMPIRE => "EMPIRE"
      case JEDI => "JEDI"
    })
  }
}