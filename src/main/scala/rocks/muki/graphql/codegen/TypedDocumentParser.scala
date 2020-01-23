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

import sangria.validation.TypeInfo
import sangria.schema._
import sangria.ast

case class TypedDocumentParser(schema: Schema[Any, Any], document: ast.Document) {

  /**
    * We need the schema to generate any type info from a parsed ast.Document
    */
  private val typeInfo = new TypeInfo(schema)

  /**
    * Aggregates all types seen during the document parsing
    */
  private val types = scala.collection.mutable.Set[Type]()

  def parse(): Result[TypedDocument.Api] = {
    val api = TypedDocument.Api(
      operations = document.operations.values.map(generateOperation).toList,
      interfaces = List.empty,
      fragments = document.fragments.values.toList.map(generateFragment),
      // Include only types that have been used in the document
      types = schema.typeList.filter(types).collect(generateType).toList,
      original = document
    )
    // generates interfaces for union types that contain all fields!
    Right(api)
  }

  /**
    * Marks a schema type so it is added to the imported AST.
    *
    * Must be explicitly called for each type that a field references. For example,
    * to generate a field which has an enum type this method should be called.
    */
  private def touchType(tpe: Type): Unit = tpe.namedType match {
    case x if types.contains(x) =>
      ()
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

  private def generateSelections(
      selections: Vector[ast.Selection],
      typeConditions: Set[Type] = Set.empty
  ): TypedDocument.Selection =
    selections
      .map(generateSelection(typeConditions))
      .foldLeft(TypedDocument.Selection.empty)(_ + _)

  private def generateSelection(
      typeConditions: Set[Type]
  )(node: ast.Selection): TypedDocument.Selection = {
    def conditionalFragment(
        f: => TypedDocument.Selection
    ): TypedDocument.Selection =
      if (typeConditions.isEmpty || typeConditions(typeInfo.tpe.get)) f else TypedDocument.Selection.empty

    typeInfo.enter(node)
    val result = node match {
      // check if this field has a codeGen directive
      case field: ast.Field if getUseTypeDirectiveValue(field).isDefined =>
        require(typeInfo.tpe.isDefined, s"Field without type: $field")
        val tpe = typeInfo.tpe.get
        // turns this grapqhl query: fieldName @codeGen(useType: "Foo") { ... }
        // into "fieldName: Foo"

        val codeGen = TypedDocument.CodeGen(useType = getUseTypeDirectiveValue(field).get)

        TypedDocument.Selection(
          fields = List(
            // the typeCondition the fragment definition (... on clause) should be the same as the field type
            TypedDocument.Field(field.outputName, tpe, codeGen = Some(codeGen))
          )
        )
      // look ahead in the selections and handle if there's only a fragment selection inside
      case field: ast.Field if hasSingleFragmentSelection(field) =>
        require(typeInfo.tpe.isDefined, s"Field without type: $field")
        val tpe = typeInfo.tpe.get
        // turns this grapqhl query: fieldName { ...FragmentName }
        // into "fieldName: FragmentName"

        val fragmentSpread = field.selections.head.asInstanceOf[ast.FragmentSpread]
        val fragment = TypedDocument.FragmentSelection(fragmentSpread.name, tpe.namedType.name)

        TypedDocument.Selection(
          fields = List(
            // the typeCondition the fragment definition (... on clause) should be the same as the field type
            TypedDocument.Field(field.outputName, tpe, fragment = Some(fragment))
          )
        )

      case field: ast.Field =>
        require(typeInfo.tpe.isDefined, s"Field without type: $field")
        val tpe = typeInfo.tpe.get

        tpe.namedType match {
          case union: UnionType[_] =>
            val types = union.types.map { tpe =>
              val conditions = Set[Type](union, tpe) ++ tpe.interfaces
              val selection = generateSelections(field.selections, conditions)
              TypedDocument.UnionSelection(tpe, selection)
            }
            TypedDocument.Selection(
              TypedDocument.Field(field.outputName, tpe, union = types)
            )

          case obj @ (_: ObjectLikeType[_, _] | _: InputObjectType[_]) =>
            val gen = generateSelections(field.selections)
            TypedDocument.Selection(
              TypedDocument.Field(field.outputName, tpe, selection = Some(gen))
            )

          case _ =>
            touchType(tpe)
            TypedDocument.Selection(TypedDocument.Field(field.outputName, tpe))
        }

      case fragmentSpread: ast.FragmentSpread =>
        val name = fragmentSpread.name
        val fragment = document.fragments(fragmentSpread.name)
        // Sangria's TypeInfo abstraction does not resolve fragment spreads
        // when traversing, so explicitly enter resolved fragment.
        typeInfo.enter(fragment)

        val result = TypedDocument.Selection(fields = List.empty)
        typeInfo.leave(fragment)

        result

      // This is only called when generateFragments is called with the documents.fragments values
      case inlineFragment: ast.InlineFragment =>
        conditionalFragment(generateSelections(inlineFragment.selections))

      case unknown =>
        sys.error("Unknown selection: " + unknown.toString)
    }
    typeInfo.leave(node)
    result
  }

  private def hasSingleFragmentSelection(field: ast.Field): Boolean =
    field.selections.length == 1 && field.selections.exists {
      case _: ast.FragmentSpread => true
      case _ => false
    }

  private def getUseTypeDirectiveValue(field: ast.Field): Option[String] =
    field.directives.collectFirst {
      case d if d.name == "codeGen" =>
        d.arguments.collectFirst {
          case ast.Argument("useType", value: ast.StringValue, _, _) => value.value
        }
    }.flatten

  private def generateOperation(
      operation: ast.OperationDefinition
  ): TypedDocument.Operation = {
    typeInfo.enter(operation)
    val variables = operation.variables.toList.map { varDef =>
      schema.getInputType(varDef.tpe) match {
        case Some(tpe) =>
          touchType(tpe)
          TypedDocument.Field(varDef.name, tpe)
        case None =>
          sys.error("Unknown input type: " + varDef.tpe)
      }
    }

    val selection = generateSelections(operation.selections)

    typeInfo.leave(operation)
    TypedDocument.Operation(operation.name, variables, selection, operation)
  }

  private def generateFragment(
      fragment: ast.FragmentDefinition
  ): TypedDocument.Fragment = {
    typeInfo.enter(fragment)

    // add all interfaces that are being used as type condition
    // all other types should be part of the Data object in the
    // generated query object.
    schema.allTypes.get(fragment.typeCondition.name).foreach {
      case schemaType: InterfaceType[_, _] => types += schemaType
      case _ =>
    }

    require(typeInfo.tpe.isDefined, s"Fragment without type: $fragment")
    val tpe = typeInfo.tpe.get
    val fieldName = fragment.name

    val field = tpe.namedType match {
      case union: UnionType[_] =>
        val types = union.types.map { tpe =>
          val conditions = Set[Type](union, tpe) ++ tpe.interfaces
          val selection = generateSelections(fragment.selections, conditions)
          TypedDocument.UnionSelection(tpe, selection)
        }
        TypedDocument.Field(fieldName, tpe, union = types)

      case _ @ (_: ObjectLikeType[_, _] | _: InputObjectType[_]) =>
        val gen = generateSelections(fragment.selections)
        TypedDocument.Field(fieldName, tpe, selection = Some(gen))

      case _ =>
        touchType(tpe)
        TypedDocument.Field(fieldName, tpe)
    }
    typeInfo.leave(fragment)

    TypedDocument.Fragment(fragment.name, field)
  }

  private def generateObject(obj: ObjectType[_, _]): TypedDocument.Object = {
    val fields = obj.uniqueFields.map { field =>
      touchType(field.fieldType)
      TypedDocument.Field(field.name, field.fieldType)
    }
    TypedDocument.Object(obj.name, fields.toList)
  }

  /**
    * Map from a sangria schema.Type to a
    * @return
    */
  private def generateType: PartialFunction[Type, TypedDocument.Type] = {
    case interface: InterfaceType[_, _] =>
      val fields = interface.uniqueFields.map { field =>
        touchType(field.fieldType)
        TypedDocument.Field(field.name, field.fieldType)
      }
      TypedDocument.Interface(interface.name, fields.toList)

    case obj: ObjectType[_, _] =>
      generateObject(obj)

    case enum: EnumType[_] =>
      val values = enum.values.map(_.name)
      TypedDocument.Enum(enum.name, values)

    case union: UnionType[_] =>
      TypedDocument.Union(union.name, union.types.map(generateObject))

    case inputObj: InputObjectType[_] =>
      val fields = inputObj.fields.map { field =>
        touchType(field.fieldType)
        TypedDocument.Field(field.name, field.fieldType)
      }
      TypedDocument.Object(inputObj.name, fields)

    case IDType =>
      TypedDocument.TypeAlias("ID", "String")
  }
}
