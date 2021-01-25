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

import sangria.{ast, schema}

/**
  * AST representing the extracted GraphQL types.
  */
sealed trait TypedDocument
object TypedDocument {

  /**
    * Selected GraphQL field
    *
    * The class contains a variety of information on a selected field to allow an source code
    * generators to define different behaviours.
    *
    * @param name - the field name
    * @param tpe - the field type extraced from the schema
    * @param selection - is None for scalar types (String, Option, List, etc.)
    * @param union contains a list of union types that are selected with the spread operator `...UnionTypeName`
    */
  case class Field(
      name: String,
      tpe: schema.Type,
      selection: Option[Selection] = None,
      union: List[UnionSelection] = List.empty,
      codeGen: Option[CodeGen] = None
  ) extends TypedDocument {
    def isObjectLike = selection.nonEmpty
    def isUnion = union.nonEmpty

    override def toString: String =
      s"Field(name:$name, type:${tpe.namedType.name}, selection: $selection , union: $union, codeGen: $codeGen)"

  }

  /**
    * A Selection represents a list of selected fields with additional meta information.
    *
    * Based on the meta information (interfaces, fragment) code generators can decide
    * what code needs to be generated.
    *
    * ## Interfaces
    *
    * The generated code may inherit the specified interfaces for this selection.
    *
    * ## Fragment
    *
    * If a fragment is the source for this selection the fragment code could be generated
    * in a separate place and referenced here by name and or type of the fragment.
    *
    * @param fields the fields that are part of the selection
    * @param interfaces the interfaces that apply to this selection
    */
  case class Selection(fields: List[Field], interfaces: List[String] = List.empty) extends TypedDocument {
    def +(that: Selection) =
      Selection((this.fields ++ that.fields).distinct, this.interfaces ++ that.interfaces)
  }
  object Selection {
    final val empty = Selection(List.empty)
    def apply(field: Field): Selection = Selection(List(field))
  }

  case class UnionSelection(tpe: schema.ObjectType[_, _], selection: Selection) extends TypedDocument {

    override def toString: String = s"UnionSelection(type: ${tpe.name}, selection: $selection)"
  }

  /**
    *
    * @param useType use this type instead and don't generate any code
    */
  case class CodeGen(useType: String) extends TypedDocument

  /**
    * Operations represent API calls and are the entry points to the API.
    *
    * @param name the operation name
    * @param variables input variables
    * @param selection the selected fields
    * @param original the original sangria OperationDefinition
    *
    */
  case class Operation(
      name: Option[String],
      variables: List[Field],
      selection: Selection,
      original: ast.OperationDefinition
  ) extends TypedDocument

  /**
    * Marker trait for GraphQL input and output types.
    */
  sealed trait Type extends TypedDocument {
    def name: String
  }
  case class Object(name: String, fields: List[Field]) extends Type
  case class Interface(name: String, fields: List[Field]) extends Type
  case class Enum(name: String, values: List[String]) extends Type
  case class TypeAlias(name: String, tpe: String) extends Type
  case class Union(name: String, types: List[Object]) extends Type

  /**
    * The API based on one or more GraphQL query documents using a given schema.
    *
    * It includes only the operations, interfaces and input/output types
    * referenced in the query documents.
    *
    * @param operations all operations
    * @param interfaces defined interfaces
    * @param types all types that are not operations related. This includes predefined types (e.g. ID)
    *              and input variable types.
    *
    */
  case class Api(operations: List[Operation], interfaces: List[Interface], types: List[Type], original: ast.Document)
}
