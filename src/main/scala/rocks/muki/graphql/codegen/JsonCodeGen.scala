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
    * @param unionTrait the union trait
    * @param unionNames all union field names
    * @param typeDiscriminatorField the field which determines the output type for union types
    * @return a json decoder instance for union types
    */
  def generateUnionFieldDecoder(unionTrait: Type.Name,
                                unionNames: List[String],
                                typeDiscriminatorField: String): List[Stat]

  /**
    *
    * @param enumTrait the enum trait
    * @param enumValues all enum field names
    * @return a json decoder instance for enum types
    */
  def generateEnumFieldDecoder(enumTrait: Type.Name,
                               enumValues: List[String]): List[Stat]

}

object JsonCodeGens {

  object None extends JsonCodeGen {
    override def imports: List[Stat] = Nil
    override def generateFieldDecoder(name: Type.Name): List[Stat] = Nil
    override def generateUnionFieldDecoder(
        unionTrait: Type.Name,
        unionNames: List[String],
        typeDiscriminatorField: String): List[Stat] = Nil

    def generateEnumFieldDecoder(enumTrait: Type.Name,
                                 enumValues: List[String]): List[Stat] = Nil
  }

  object Circe extends JsonCodeGen {
    override def imports: List[Stat] = List(
      q"import io.circe.Decoder",
      q"import io.circe.generic.semiauto.deriveDecoder"
    )

    override def generateFieldDecoder(name: Type.Name): List[Stat] = List(
      q"implicit val jsonDecoder: Decoder[$name] = deriveDecoder[$name]"
    )

    override def generateUnionFieldDecoder(
        unionTrait: Type.Name,
        unionNames: List[String],
        typeDiscriminatorField: String): List[Stat] = {
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

    override def generateEnumFieldDecoder(
        enumTrait: Type.Name,
        enumValues: List[String]): List[Stat] = {
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
  }

  object PlayJson extends JsonCodeGen {
    override def imports: List[Stat] = List(
      q"import play.api.libs.json._",
      q"import play.api.libs.json.Json._"
    )

    override def generateFieldDecoder(name: Type.Name): List[Stat] = List(
      q"implicit val jsonDecoder: Reads[$name] = reads[$name]"
    )

    override def generateUnionFieldDecoder(
      unionTrait: Type.Name,
      unionNames: List[String],
      typeDiscriminatorField: String): List[Stat] = {

      val discriminatorFieldLiteral = Lit.String(typeDiscriminatorField)
      val patterns = unionNames.map { name =>
        val nameLiteral = Lit.String(name)
        val nameType = Type.Name(name)
        p"case $nameLiteral => jsValue.validate[$nameType]"
      } ++ List(
        p"""case other => JsError("invalid type: " + other)"""
      )

      List(
        q"""
        implicit val jsonDecoder: Reads[$unionTrait] = jsValue => {
                    (jsValue \ $discriminatorFieldLiteral).validate[String] match {
                      case JsSuccess(typeTag, _) => typeTag match { ..case $patterns }
                      case err @ JsError(_) => err
                    }
                  }
       """)

    }

    override def generateEnumFieldDecoder(enumTrait: Type.Name, enumValues: List[String]): List[Stat] = {
      val patterns: List[Case] = enumValues.map { name =>
        val nameLiteral = Lit.String(name)
        val objName = Term.Name(enumTrait.value)
        val memberName = Term.Name(name)
        val selection = q"$objName.$memberName"

        p"case $nameLiteral => JsSuccess($selection)"
      } ++ List(
        p"""case other => JsError("invalid enum value: " + other)"""
      )

      q"""implicit val jsonDecoder: Reads[$enumTrait] = Reads[$enumTrait](_.validate[String].flatMap({ ..case $patterns })) """ :: Nil
    }
  }
}
