package rocks.muki.graphql.codegen

import scala.meta._

/**
  * == JsonCodeGen ==
  *
  */
trait JsonCodeGen {

  /**
    * @return necessary imports for this json codec
    */
  def imports: List[Stat]

  /**
    *
    * @param name the field name
    * @return a json decoder instance
    */
  def generateFieldDecoder(name: Type.Name): List[Stat]

}

object JsonCodeGens {

  object None extends JsonCodeGen {
    override def imports: List[Stat] = Nil
    override def generateFieldDecoder(name: Type.Name): List[Stat] = Nil
  }

  object Circe extends JsonCodeGen {
    override def imports: List[Stat] = List(
      q"import io.circe.Decoder",
      q"import io.circe.generic.semiauto.deriveDecoder"
    )

    override def generateFieldDecoder(name: Type.Name): List[Stat] = List(
      q"implicit val jsonDecoder: Decoder[$name] = deriveDecoder[$name]"
    )
  }
}
