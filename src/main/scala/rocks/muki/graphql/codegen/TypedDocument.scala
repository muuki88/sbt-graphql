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
    * Selcted GraphQL field
    *
    * @param name - the field name
    * @param tpe - the field type extraced from the schema
    * @param selection -
    * @param union
    */
  case class Field(name: String,
                   tpe: schema.Type,
                   selection: Option[Selection] = None,
                   union: List[UnionSelection] = List.empty)
      extends TypedDocument {
    def isObjectLike = selection.nonEmpty
    def isUnion = union.nonEmpty
  }

  case class Selection(fields: List[Field],
                       interfaces: List[String] = List.empty)
      extends TypedDocument {
    def +(that: Selection) =
      Selection((this.fields ++ that.fields).distinct,
                this.interfaces ++ that.interfaces)
  }
  object Selection {
    final val empty = Selection(List.empty)
    def apply(field: Field): Selection =
      Selection(List(field))
  }

  case class UnionSelection(tpe: schema.ObjectType[_, _], selection: Selection)
      extends TypedDocument

  /**
    * Operations represent API calls and are the entry points to the API.
    *
    * @param name the operation name
    * @param variables input variables
    * @param selection the selected fields
    * @param original the original sangria OperationDefinition
    *
    */
  case class Operation(name: Option[String],
                       variables: List[Field],
                       selection: Selection,
                       original: ast.OperationDefinition)
      extends TypedDocument

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
  case class Api(operations: List[Operation],
                 interfaces: List[Interface],
                 types: List[Type])
}
