import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
object types {
  sealed trait Episode
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
  }
}
