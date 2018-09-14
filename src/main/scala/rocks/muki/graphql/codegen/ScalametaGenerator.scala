/*
 * Copyright 2017 Mediative
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rocks.muki.graphql.codegen

import scala.meta._
import sangria.schema

/**
  * Generate code using Scalameta.
  */
case class ScalametaGenerator(moduleName: Term.Name,
                              emitInterfaces: Boolean = false,
                              stats: List[Stat] = List.empty)
    extends Generator[Defn.Object] {

  override def apply(api: TypedDocument.Api): Result[Defn.Object] = {
    val operations = api.operations.flatMap(generateOperation)
    val fragments =
      if (emitInterfaces)
        api.interfaces.map(generateInterface)
      else
        List.empty
    val types = api.types.flatMap(generateType)

    Right(
      q"""
        object $moduleName {
          ..$operations
          ..$fragments
          ..$types
          ..$stats
        }
      """
    )
  }

  def termParam(paramName: String, tpe: Type) =
    Term.Param(List.empty, Term.Name(paramName), Some(tpe), None)

  def generateTemplate(traits: List[String],
                       prefix: String = moduleName.value + "."): Template = {
    // TODO fix constructor names
    val templateInits = traits
      .map(prefix + _)
      .map(name => Init(ScalametaUtils.typeRefOf(name), Name.Anonymous(), Nil))
    val emptySelf = Self(Name.Anonymous(), None)

    Template(Nil, templateInits, emptySelf, List.empty)
  }

  def generateFieldType(field: TypedDocument.Field)(
      genType: schema.Type => Type): Type = {
    def typeOf(tpe: schema.Type): Type = tpe match {
      case schema.OptionType(wrapped) =>
        t"Option[${typeOf(wrapped)}]"
      case schema.OptionInputType(wrapped) =>
        t"Option[${typeOf(wrapped)}]"
      case schema.ListType(wrapped) =>
        t"List[${typeOf(wrapped)}]"
      case schema.ListInputType(wrapped) =>
        t"List[${typeOf(wrapped)}]"
      case tpe: schema.ScalarType[_] if tpe == schema.IDType =>
        ScalametaUtils.typeRefOf(moduleName.value + ".ID")
      case tpe: schema.Type =>
        genType(tpe)
    }
    typeOf(field.tpe)
  }

  def generateOperation(operation: TypedDocument.Operation): List[Stat] = {
    def fieldType(field: TypedDocument.Field, prefix: String = ""): Type =
      generateFieldType(field) { tpe =>
        if (field.isObjectLike || field.isUnion)
          ScalametaUtils.typeRefOf(prefix + field.name.capitalize)
        else
          ScalametaUtils.typeRefOf(tpe.namedType.name)
      }

    def generateSelectionParams(prefix: String)(
        selection: TypedDocument.Selection): List[Term.Param] =
      selection.fields.map { field =>
        val tpe = fieldType(field, prefix)
        termParam(field.name, tpe)
      }

    def generateSelectionStats(prefix: String)(
        selection: TypedDocument.Selection): List[Stat] =
      selection.fields.flatMap {
        // render enumerations (union types)
        case TypedDocument.Field(name, tpe, None, unionTypes)
            if unionTypes.nonEmpty =>
          val unionName = Type.Name(name.capitalize)
          val objectName = Term.Name(unionName.value)
          val template = generateTemplate(List(unionName.value), prefix)
          val unionValues = unionTypes.flatMap {
            case TypedDocument.UnionSelection(unionType, unionSelection) =>
              val path = prefix + unionName.value + "." + unionType.name + "."
              val stats = generateSelectionStats(path)(unionSelection)
              val params = generateSelectionParams(path)(unionSelection)
              val tpeName = Type.Name(unionType.name)
              val termName = Term.Name(unionType.name)

              List(q"case class $tpeName(..$params) extends $template") ++
                Option(stats)
                  .filter(_.nonEmpty)
                  .map { stats =>
                    q"object $termName { ..$stats }"
                  }
                  .toList
          }

          List[Stat](
            q"sealed trait $unionName",
            q"object $objectName { ..$unionValues }"
          )

        // render a nested case class for a deeper selection
        case TypedDocument.Field(name, tpe, Some(selection), _) =>
          val stats =
            generateSelectionStats(prefix + name.capitalize + ".")(selection)
          val params =
            generateSelectionParams(prefix + name.capitalize + ".")(selection)

          val tpeName = Type.Name(name.capitalize)
          val termName = Term.Name(name.capitalize)
          val interfaces =
            if (emitInterfaces) selection.interfaces
            else List.empty
          val template = generateTemplate(interfaces)

          List(q"case class $tpeName(..$params) extends $template") ++
            Option(stats)
              .filter(_.nonEmpty)
              .map { stats =>
                q"object $termName { ..$stats }"
              }
              .toList

        case TypedDocument.Field(_, _, _, _) =>
          List.empty
      }

    val variables = operation.variables.map { varDef =>
      termParam(varDef.name, fieldType(varDef))
    }

    val name = operation.name.getOrElse(sys.error("found unnamed operation"))
    val prefix = moduleName.value + "." + name + "."
    val stats = generateSelectionStats(prefix)(operation.selection)
    val params = generateSelectionParams(prefix)(operation.selection)

    val tpeName = Type.Name(name)
    val termName = Term.Name(name)
    val variableTypeName = Type.Name(name + "Variables")

    List[Stat](
      q"case class $tpeName(..$params)",
      q"""
        object $termName {
          case class $variableTypeName(..$variables)
          ..$stats
        }
      """
    )
  }

  def generateInterface(interface: TypedDocument.Interface): Stat = {
    val defs = interface.fields.map { field =>
      val fieldName = Term.Name(field.name)
      val tpe = generateFieldType(field) { tpe =>
        field.selection.map(_.interfaces).filter(_.nonEmpty) match {
          case Some(interfaces) =>
            interfaces.map(x => Type.Name(x): Type).reduce(Type.With(_, _))
          case None =>
            Type.Name(tpe.namedType.name)
        }
      }
      q"def $fieldName: $tpe"
    }
    val traitName = Type.Name(interface.name)
    q"trait $traitName { ..$defs }"
  }

  def generateObject(obj: TypedDocument.Object,
                     interfaces: List[String]): Stat = {
    val params = obj.fields.map { field =>
      val tpe = generateFieldType(field)(t => Type.Name(t.namedType.name))
      termParam(field.name, tpe)
    }
    val className = Type.Name(obj.name)
    val template = generateTemplate(interfaces)
    q"case class $className(..$params) extends $template": Stat
  }

  def generateType(tree: TypedDocument.Type): List[Stat] = tree match {
    case interface: TypedDocument.Interface =>
      if (emitInterfaces)
        List(generateInterface(interface))
      else
        List.empty

    case obj: TypedDocument.Object =>
      List(generateObject(obj, List.empty))

    case TypedDocument.Enum(name, values) =>
      val enumValues = values.map { value =>
        val template = generateTemplate(List(name))
        val valueName = Term.Name(value)
        q"case object $valueName extends $template"
      }

      val enumName = Type.Name(name)
      val objectName = Term.Name(name)
      List[Stat](
        q"sealed trait $enumName",
        q"object $objectName { ..$enumValues }"
      )

    case TypedDocument.TypeAlias(from, to) =>
      val alias = Type.Name(from)
      val underlying = Type.Name(to)
      List(q"type $alias = $underlying": Stat)

    case TypedDocument.Union(name, types) =>
      val unionValues = types.map(obj => generateObject(obj, List(name)))
      val unionName = Type.Name(name)
      val objectName = Term.Name(name)
      List[Stat](
        q"sealed trait $unionName",
        q"object $objectName { ..$unionValues }"
      )
  }

}

object ScalametaGenerator {
  def apply(moduleName: String): ScalametaGenerator =
    ScalametaGenerator(Term.Name(moduleName))
}
