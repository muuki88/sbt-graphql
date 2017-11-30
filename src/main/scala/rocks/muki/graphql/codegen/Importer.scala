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

import scala.collection.immutable.Seq
import sangria.validation.TypeInfo
import sangria.schema._
import sangria.ast

case class Importer(schema: Schema[_, _], document: ast.Document) {
  private val typeInfo = new TypeInfo(schema)
  private val types    = scala.collection.mutable.Set[Type]()

  def parse(): Result[Tree.Api] =
    Right(
      Tree.Api(
	document.operations.values.map(generateOperation).toVector,
	document.fragments.values.toVector.map(generateFragment),
	schema.typeList.filter(types).collect(generateType)
      ))

  /**
   * Marks a schema type so it is added to the imported AST.
   *
   * Must be explicitly called for each type that a field references. For example,
   * to generate a field which has an enum type this method should be called.
   */
  def touchType(tpe: Type): Unit = tpe.namedType match {
    case IDType =>
      types += tpe
      ()
    case _: ScalarType[_] =>
      // Nothing
      ()
    case input: InputObjectType[_] =>
      types += input
      input.fields.foreach(field => touchType(field.fieldType))
    case union: UnionType[_] =>
      types += union
      union.types.flatMap(_.fields.map(_.fieldType)).foreach(touchType)
    case underlying: OutputType[_] =>
      types += underlying
      ()
  }

  def generateSelections(
      selections: Seq[ast.Selection],
      typeConditions: Set[Type] = Set.empty): Tree.Selection =
    selections.map(generateSelection(typeConditions)).foldLeft(Tree.Selection.empty)(_ + _)

  def generateSelection(typeConditions: Set[Type])(node: ast.Selection): Tree.Selection = {
    def conditionalFragment(f: => Tree.Selection): Tree.Selection =
      if (typeConditions.isEmpty || typeConditions(typeInfo.tpe.get))
	f
      else
	Tree.Selection.empty

    typeInfo.enter(node)
    val result = node match {
      case field: ast.Field =>
	require(typeInfo.tpe.isDefined, s"Field without type: $field")
	val tpe = typeInfo.tpe.get
	tpe.namedType match {
	  case union: UnionType[_] =>
	    val types = union.types.toList.map { tpe =>
	      // Prepend the union type name to include and descend into fragment spreads
	      val conditions = Set[Type](union, tpe) ++ tpe.interfaces
	      val selection  = generateSelections(field.selections, conditions)
	      Tree.UnionSelection(tpe, selection)
	    }
	    Tree.Selection(Tree.Field(field.outputName, tpe, union = types))

	  case obj @ (_: ObjectLikeType[_, _] | _: InputObjectType[_]) =>
	    val gen = generateSelections(field.selections)
	    Tree.Selection(Tree.Field(field.outputName, tpe, selection = Some(gen)))

	  case _ =>
	    touchType(tpe)
	    Tree.Selection(Tree.Field(field.outputName, tpe))
	}

      case fragmentSpread: ast.FragmentSpread =>
	val name     = fragmentSpread.name
	val fragment = document.fragments(fragmentSpread.name)
	// Sangria's TypeInfo abstraction does not resolve fragment spreads
	// when traversing, so explicitly enter resolved fragment.
	typeInfo.enter(fragment)
	val result = conditionalFragment(
	  generateSelections(fragment.selections, typeConditions).copy(interfaces = Vector(name)))
	typeInfo.leave(fragment)
	result

      case inlineFragment: ast.InlineFragment =>
	conditionalFragment(generateSelections(inlineFragment.selections))

      case unknown =>
	sys.error("Unknown selection: " + unknown.toString)
    }
    typeInfo.leave(node)
    result
  }

  def generateOperation(operation: ast.OperationDefinition): Tree.Operation = {
    typeInfo.enter(operation)
    val variables = operation.variables.toVector.map { varDef =>
      schema.getInputType(varDef.tpe) match {
	case Some(tpe) =>
	  touchType(tpe)
	  Tree.Field(varDef.name, tpe)
	case None =>
	  sys.error("Unknown input type: " + varDef.tpe)
      }
    }

    val selection = generateSelections(operation.selections)
    typeInfo.leave(operation)
    Tree.Operation(operation.name, variables, selection)
  }

  def generateFragment(fragment: ast.FragmentDefinition): Tree.Interface = {
    typeInfo.enter(fragment)
    val selection = generateSelections(fragment.selections)
    typeInfo.leave(fragment)
    Tree.Interface(fragment.name, selection.fields)
  }

  def generateObject(obj: ObjectType[_, _]): Tree.Object = {
    val fields = obj.uniqueFields.map { field =>
      touchType(field.fieldType)
      Tree.Field(field.name, field.fieldType)
    }
    Tree.Object(obj.name, fields)
  }

  def generateType: PartialFunction[Type, Tree.Type] = {
    case interface: InterfaceType[_, _] =>
      val fields = interface.uniqueFields.map { field =>
	touchType(field.fieldType)
	Tree.Field(field.name, field.fieldType)
      }
      Tree.Interface(interface.name, fields)

    case obj: ObjectType[_, _] =>
      generateObject(obj)

    case enum: EnumType[_] =>
      val values = enum.values.map(_.name)
      Tree.Enum(enum.name, values)

    case union: UnionType[_] =>
      Tree.Union(union.name, union.types.map(generateObject))

    case inputObj: InputObjectType[_] =>
      val fields = inputObj.fields.map { field =>
	touchType(field.fieldType)
	Tree.Field(field.name, field.fieldType)
      }
      Tree.Object(inputObj.name, fields)

    case IDType =>
      Tree.TypeAlias("ID", "String")
  }
}
