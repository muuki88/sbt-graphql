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

  /**
    *
    * @param name the field name
    * @return a json encoder instance
    */
  def generateFieldEncoder(name: Type.Name): List[Stat]

  /**
    *
    * @param unionTrait the union trait
    * @param unionNames all union field names
    * @param typeDiscriminatorField the field which determines the output type for union types
    * @return a json decoder instance for union types
    */
  def generateUnionFieldDecoder(
      unionTrait: Type.Name,
      unionNames: List[String],
      typeDiscriminatorField: String
  ): List[Stat]

  /**
    *
    * @param unionTrait the union trait
    * @param unionNames all union field names
    * @param typeDiscriminatorField the field which determines the output type for union types
    * @return a json encoder instance for union types
    */
  def generateUnionFieldEncoder(
      unionTrait: Type.Name,
      unionNames: List[String],
      typeDiscriminatorField: String
  ): List[Stat]

  /**
    *
    * @param enumTrait the enum trait
    * @param enumValues all enum field names
    * @return a json decoder instance for enum types
    */
  def generateEnumFieldDecoder(enumTrait: Type.Name, enumValues: List[String]): List[Stat]

  /**
    *
    * @param enumTrait the enum trait
    * @param enumValues all enum field names
    * @return a json decoder instance for enum types
    */
  def generateEnumFieldEncoder(enumTrait: Type.Name, enumValues: List[String]): List[Stat]

}

object JsonCodeGens {

  object None extends JsonCodeGen {
    override def imports: List[Stat] = Nil
    override def generateFieldDecoder(name: Type.Name): List[Stat] = Nil
    override def generateFieldEncoder(name: Type.Name): List[Stat] = Nil
    override def generateUnionFieldDecoder(
        unionTrait: Type.Name,
        unionNames: List[String],
        typeDiscriminatorField: String
    ): List[Stat] = Nil

    override def generateUnionFieldEncoder(
        unionTrait: Type.Name,
        unionNames: List[String],
        typeDiscriminatorField: String
    ): List[Stat] = Nil

    def generateEnumFieldDecoder(enumTrait: Type.Name, enumValues: List[String]): List[Stat] = Nil
    def generateEnumFieldEncoder(enumTrait: Type.Name, enumValues: List[String]): List[Stat] = Nil
  }

  object Circe extends JsonCodeGen {
    override def imports: List[Stat] = List(
      q"import io.circe.{Decoder, Encoder}",
      q"import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}"
    )

    override def generateFieldDecoder(name: Type.Name): List[Stat] = List(
      q"implicit val jsonDecoder: Decoder[$name] = deriveDecoder[$name]"
    )

    override def generateFieldEncoder(name: Type.Name): List[Stat] = List(
      q"implicit val jsonEncoder: Encoder[$name] = deriveEncoder[$name]"
    )

    override def generateUnionFieldDecoder(
        unionTrait: Type.Name,
        unionNames: List[String],
        typeDiscriminatorField: String
    ): List[Stat] = {
      val discriminatorFieldLiteral = Lit.String(typeDiscriminatorField)
      val patterns = unionNames.map { name =>
        val nameLiteral = Lit.String(name)
        val nameType = Type.Name(name)
        p"case $nameLiteral => Decoder[$nameType]"
      } ++ List(
        p"""case other => Decoder.failedWithMessage("invalid type: " + other)"""
      )

      List(q"""
        implicit val jsonDecoder: Decoder[$unionTrait] = for {
          typeDiscriminator <- Decoder[String].prepare(_.downField($discriminatorFieldLiteral))
          value <- typeDiscriminator match { ..case $patterns }
        } yield value
       """)
    }

    /**
      * GraphQL doesn't support unions in arguments (only in responses), so generating an Encoder might seem silly
      * at first. It is still needed if `deriveEncoder` in generated code references a union (otherwise the compilation
      * fails) or if you want to generate json responses using the generated code (e.g. in tests for your application).
      *
      * To match the Decoder we pattern match on the `unionTrait` and produce a json object using `deriveEncoder` based
      * on the case we have. This avoids the default nesting of sum-types `circe` does. The discriminator field
      * `__typename` is automatically included via `deriveEncoder` as the generated code for each case includes it as
      * normal field already.
      *
      * We could make sure that the value for `__typename` is always the name of the type, ignoring the value someone
      * put in the field of the case class. As of now, we don't do that, so test-code potentially has to do this
      * manually but is also not bound to a hardcoded value in the Encoder here (e.g. for testing invalid responses).
      */
    override def generateUnionFieldEncoder(
        unionTrait: Type.Name,
        unionNames: List[String],
        typeDiscriminatorField: String
    ): List[Stat] = {
      val patterns = unionNames.map { name =>
        val typeName = Type.Name(name)

        p"case v: $typeName => deriveEncoder[$typeName].apply(v)"
      }

      List(
        q"""implicit val jsonEncoder: Encoder[$unionTrait] = Encoder.instance[$unionTrait] {
              ..case $patterns
            } """
      )
    }

    override def generateEnumFieldDecoder(enumTrait: Type.Name, enumValues: List[String]): List[Stat] = {
      val patterns = enumValues.map { name =>
        val nameLiteral = Lit.String(name)
        val enumTerm = Term.Name(name)
        p"case $nameLiteral => Right($enumTerm)"
      } ++ List(
        p"""case other => Left("invalid enum value: " + other)"""
      )

      List(q"""
        implicit val jsonDecoder: Decoder[$enumTrait] = Decoder.decodeString.emap {
            ..case $patterns
        } """)
    }

    override def generateEnumFieldEncoder(
        enumTrait: Type.Name,
        enumValues: List[String]
    ): List[Stat] = {
      val patterns = enumValues.map { name =>
        val nameLiteral = Lit.String(name)
        val enumTerm = Term.Name(name)
        p"case $enumTerm => $nameLiteral"
      }

      List(q"""
        implicit val jsonEncoder: Encoder[$enumTrait] = Encoder.encodeString.contramap {
            ..case $patterns
        } """)
    }
  }
}
