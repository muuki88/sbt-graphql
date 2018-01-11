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
import scala.meta._
import sangria.schema

/**
  * Generate code using Scalameta.
  */
case class ScalametaGenerator(moduleName: Term.Name,
			      emitInterfaces: Boolean = false,
			      stats: Seq[Stat] = Vector.empty)
    extends Generator[Defn.Object] {

  override def apply(api: Tree.Api): Result[Defn.Object] = {
    val operations = api.operations.flatMap(generateOperation)
    val fragments =
      if (emitInterfaces)
	api.interfaces.map(generateInterface)
      else
	Seq.empty
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
    Term.Param(Vector.empty, Term.Name(paramName), Some(tpe), None)

  def generateTemplate(traits: Seq[String],
		       prefix: String = moduleName.value + "."): Template = {
    val ctorNames = traits.map(prefix + _).map(Ctor.Name.apply)
    val emptySelf = Term.Param(Vector.empty, Name.Anonymous(), None, None)
    Template(Nil, ctorNames, emptySelf, None)
  }

  def generateFieldType(field: Tree.Field)(
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
	Type.Name(moduleName.value + ".ID")
      case tpe: schema.Type =>
	genType(tpe)
    }
    typeOf(field.tpe)
  }

  def generateOperation(operation: Tree.Operation): Seq[Stat] = {
    def fieldType(field: Tree.Field, prefix: String = ""): Type =
      generateFieldType(field) { tpe =>
	if (field.isObjectLike || field.isUnion)
	  Type.Name(prefix + field.name.capitalize)
	else
	  Type.Name(tpe.namedType.name)
      }

    def generateSelectionParams(prefix: String)(
	selection: Tree.Selection): Seq[Term.Param] =
      selection.fields.map { field =>
	val tpe = fieldType(field, prefix)
	termParam(field.name, tpe)
      }

    def generateSelectionStats(prefix: String)(
	selection: Tree.Selection): Seq[Stat] =
      selection.fields.flatMap {
	case Tree.Field(name, tpe, None, unionTypes) if unionTypes.nonEmpty =>
	  val unionName = Type.Name(name.capitalize)
	  val objectName = Term.Name(unionName.value)
	  val template = generateTemplate(Seq(unionName.value), prefix)
	  val unionValues = unionTypes.flatMap {
	    case Tree.UnionSelection(tpe, selection) =>
	      val path = prefix + unionName.value + "." + tpe.name + "."
	      val stats = generateSelectionStats(path)(selection)
	      val params = generateSelectionParams(path)(selection)
	      val tpeName = Type.Name(tpe.name)
	      val termName = Term.Name(tpe.name)

	      Vector(q"case class $tpeName(..$params) extends $template") ++
		Option(stats)
		  .filter(_.nonEmpty)
		  .map { stats =>
		    q"object $termName { ..$stats }"
		  }
		  .toVector
	  }

	  Vector[Stat](
	    q"sealed trait $unionName",
	    q"object $objectName { ..$unionValues }"
	  )

	case Tree.Field(name, tpe, Some(selection), _) =>
	  val stats =
	    generateSelectionStats(prefix + name.capitalize + ".")(selection)
	  val params =
	    generateSelectionParams(prefix + name.capitalize + ".")(selection)

	  val tpeName = Type.Name(name.capitalize)
	  val termName = Term.Name(name.capitalize)
	  val interfaces =
	    if (emitInterfaces) selection.interfaces
	    else Seq.empty
	  val template = generateTemplate(interfaces)

	  Vector(q"case class $tpeName(..$params) extends $template") ++
	    Option(stats)
	      .filter(_.nonEmpty)
	      .map { stats =>
		q"object $termName { ..$stats }"
	      }
	      .toVector

	case Tree.Field(_, _, _, _) =>
	  Vector.empty
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

    Vector[Stat](
      q"case class $tpeName(..${params})",
      q"""
	object $termName {
	  case class $variableTypeName(..$variables)
	  ..${stats}
	}
      """
    )
  }

  def generateInterface(interface: Tree.Interface): Stat = {
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

  def generateObject(obj: Tree.Object, interfaces: Seq[String]): Stat = {
    val params = obj.fields.map { field =>
      val tpe = generateFieldType(field)(t => Type.Name(t.namedType.name))
      termParam(field.name, tpe)
    }
    val className = Type.Name(obj.name)
    val template = generateTemplate(interfaces)
    q"case class $className(..$params) extends $template": Stat
  }

  def generateType(tree: Tree.Type): Seq[Stat] = tree match {
    case interface: Tree.Interface =>
      if (emitInterfaces)
	Vector(generateInterface(interface))
      else
	Vector.empty

    case obj: Tree.Object =>
      Vector(generateObject(obj, Seq.empty))

    case Tree.Enum(name, values) =>
      val enumValues = values.map { value =>
	val template = generateTemplate(Seq(name))
	val valueName = Term.Name(value)
	q"case object $valueName extends $template"
      }

      val enumName = Type.Name(name)
      val objectName = Term.Name(name)
      Vector[Stat](
	q"sealed trait $enumName",
	q"object $objectName { ..$enumValues }"
      )

    case Tree.TypeAlias(from, to) =>
      val alias = Type.Name(from)
      val underlying = Type.Name(to)
      Vector(q"type $alias = $underlying": Stat)

    case Tree.Union(name, types) =>
      val unionValues = types.map(obj => generateObject(obj, Seq(name)))
      val unionName = Type.Name(name)
      val objectName = Term.Name(name)
      Vector[Stat](
	q"sealed trait $unionName",
	q"object $objectName { ..$unionValues }"
      )
  }

}

object ScalametaGenerator {
  def apply(moduleName: String): ScalametaGenerator =
    ScalametaGenerator(Term.Name(moduleName))
}
