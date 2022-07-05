import play.api.libs.json.{ Json, Reads, Writes, JsValue, JsObject, JsString, JsSuccess, JsError }
object types {
  sealed trait Episode extends Product with Serializable
  object Episode {
    case object NEWHOPE extends Episode
    case object EMPIRE extends Episode
    case object JEDI extends Episode
    implicit val jsonReads: Reads[Episode] = {
      case JsString("NEWHOPE") =>
        JsSuccess(NEWHOPE)
      case JsString("EMPIRE") =>
        JsSuccess(EMPIRE)
      case JsString("JEDI") =>
        JsSuccess(JEDI)
      case other =>
        JsError("Invalid " + "Episode" + ": " + other)
    }
    implicit val jsonWrites: Writes[Episode] = {
      case NEWHOPE =>
        JsString("NEWHOPE")
      case EMPIRE =>
        JsString("EMPIRE")
      case JEDI =>
        JsString("JEDI")
    }
  }
}