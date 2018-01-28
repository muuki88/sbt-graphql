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

import sangria.schema

import scala.meta._

/**
  * Generate code using Scalameta.
  */
case class NeoScalametaGenerator(fileName: String, additionalImports: List[Import]) extends Generator[List[Stat]] {


  private val GraphQLQueryTrait: Defn.Trait =
    q"""trait GraphQLQuery {
          type Document
          type Variables
          type Data
        }
     """

  private val GraphQLQuery: Init = Init(Type.Name("GraphQLQuery"), Name.Anonymous(), Nil)


  override def apply(document: TypedDocument.Api): Result[List[Stat]] = {

    // TODO refactor Generator trait into something more flexible


    val operations = document.operations.map { operation =>
        val typeName = Term.Name(operation.name.getOrElse(throw new IllegalArgumentException("Anonymous operations are not support")))
        // TODO input variables can be recursive. Generate case classes along
        val inputParams = generateFieldParams(operation.variables, List.empty)
        val dataParams = generateFieldParams(operation.selection.fields, List.empty)
        val data = operation.selection.fields.flatMap(selectionStats(_, List.empty))

        // render the document into the query object.
        // replacing single $ with $$ for escaping
        val escapedDocumentString = operation.original.renderPretty.replaceAll("\\$", "\\$\\$")
        val document = Term.Interpolate(Term.Name("graphql"), Lit.String(escapedDocumentString) :: Nil, Nil)

        q"""
          object $typeName extends $GraphQLQuery {
           val Document = $document
           case class Variables(..$inputParams)
           case class Data(..$dataParams)
           ..$data
          }"""
    }
    val interfaces = document.interfaces.map(generateInterface)

    Right(
      additionalImports ++
      List(
      q"import sangria.macros._",
      q"""
       object ${Term.Name(fileName)} {
          ..$operations
          ..$interfaces
       }
     """))
  }

  private def selectionStats(field: TypedDocument.Field, typeQualifiers: List[String]): List[Stat] = field match {
    // render enumerations (union types)
    case TypedDocument.Field(name, _, None, unionTypes) if unionTypes.nonEmpty =>
      // create the union types

      val unionName = Type.Name(name.capitalize)
      val unionCompanionObject = Term.Name(unionName.value)
      val unionTrait = generateTemplate(List(unionName.value))

      // create concrete case classes for each union type
      val unionValues = unionTypes.flatMap {
        case TypedDocument.UnionSelection(unionType, unionSelection) =>
          // get nested selections
          val innerSelections = unionSelection.fields.flatMap(field => selectionStats(field, List.empty))
          val params = generateFieldParams(unionSelection.fields, typeQualifiers :+ unionName.value)
          val unionTypeName = Type.Name(unionType.name)
          val unionTermName = Term.Name(unionType.name)

          List(q"case class $unionTypeName(..$params) extends $unionTrait") ++
            Option(innerSelections)
              .filter(_.nonEmpty)
              .map { stats =>
                q"object $unionTermName { ..$stats }"
              }
              .toList
      }

      List[Stat](
        q"sealed trait $unionName",
        q"object $unionCompanionObject { ..$unionValues }"
      )

    // render a nested case class for a deeper selection
    case TypedDocument.Field(name, tpe, Some(fieldSelection), _) =>
      // Recursive call - create more case classes

      val fieldName = Type.Name(name.capitalize)
      val termName = Term.Name(name.capitalize)
      val template = generateTemplate(fieldSelection.interfaces)

      // The inner stats don't require the typeQualifiers as they are packed into a separate
      // object, which is like a fresh start.
      val innerStats = fieldSelection.fields.flatMap(selectionStats(_, List.empty))

      // Add
      val params = generateFieldParams(fieldSelection.fields, typeQualifiers :+ termName.value)
      List(
        // "// nice comment".parse[Stat].get,
        q"case class $fieldName(..$params) extends $template"
      ) ++ Option(innerStats).filter(_.nonEmpty).map {
        stats => q"object $termName { ..$stats }"
      }
    case TypedDocument.Field(_, _, _, _) =>
      // scalar types, e.g. String, Option, List
      List.empty
  }


  private def generateFieldParams(fields: List[TypedDocument.Field], typeQualifiers: List[String]): List[Term.Param] =
    fields.map { field =>
      val tpe = parameterFieldType(field, typeQualifiers)
      termParam(field.name, tpe)
    }

  private def termParam(paramName: String, tpe: Type) =
    Term.Param(List.empty, Term.Name(paramName), Some(tpe), None)

  /**
    * Turns a Type
    * @param field
    * @return
    */
  private def parameterFieldType(field: TypedDocument.Field, typeQualifiers: List[String]): Type =
    generateFieldType(field) { tpe =>
      if (field.isObjectLike || field.isUnion) {
        // prepend the type qualifier for nested object/case class structures
        Type.Name((typeQualifiers :+ field.name.capitalize).mkString("."))
      } else {
        // this branch handles non-enum or case class types, which means we don't need the
        // typeQualifiers here.
        Type.Name(tpe.namedType.name)
      }
    }

  /**
    * Generates a scala meta Type from a TypeDocument.Field
    *
    * @param field
    * @param genType
    * @return scala type
    */
  private def generateFieldType(field: TypedDocument.Field)(genType: schema.Type => Type): Type = {
    // recursive function
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
        Type.Name("ID")
      case tpe: schema.Type =>
        genType(tpe)
    }
    typeOf(field.tpe)
  }


  private def generateTemplate(traits: List[String]): Template = {

    // val ctorNames = traits.map(Ctor.Name.apply)
    val emptySelf = Self(Name.Anonymous(), None)
    val templateInits = traits.map(name => Init(Type.Name(name), Name.Anonymous(), Nil))
    Template(early = Nil, inits = templateInits, emptySelf, stats = Nil)
  }

  private def generateInterface(interface: TypedDocument.Interface): Stat = {
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

}
