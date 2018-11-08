package rocks.muki.graphql.codegen

import scala.meta._

/**
  * More robust way to parse [[Type.Ref]] and [[Term.Ref]] from String.
  * @see More discussion: https://gitter.im/scalameta/scalameta?at=5b9ba14f8909f71f75d1b4bd
  * @see https://astexplorer.net/#/gist/ec56167ffafb20cbd8d68f24a37043a9/677e43f3adb93db8513dbe4e2c868dd4f78df4b3
  */
object ScalametaUtils {

  def typeRefOf(typeTerm: String): Type.Ref = {
    typeTerm.parse[Type].get.asInstanceOf[Type.Ref]
  }

  def typeRefOf(term: String, typeTerm: String): Type.Ref = {
    typeRefOf(term.split('.'), typeTerm)
  }

  def typeRefOf(terms: Seq[String], typeTerm: String): Type.Ref = {
    if (terms.isEmpty) {
      Type.Name(typeTerm)
    } else {
      Type.Select(termRefOf(terms), Type.Name(typeTerm))
    }
  }

  // terms must not be empty
  def termRefOf(terms: Seq[String]): Term.Ref = {
    val termArray = terms.map(Term.Name(_))
    termArray.headOption.fold(
      throw new IllegalStateException("Term must not be empty")
    ) { head =>
      termArray.drop(1).foldLeft[Term.Ref](head)((r, t) => Term.Select(r, t))
    }
  }

  def termRefOf(term: String): Term.Ref = {
    termRefOf(term.split('.'))
  }

  /**
    * Takes a list of import declarations and generates a list of scalameta import statements.
    *
    * @example {{{
    *    importer(List("java.time._", "org.apache._")
    * }}}
    *
    * @param imports a list of packages, objects or classes to import
    * @return a list of scalameta import statements
    */
  def imports(imports: List[String]): List[Import] = {
    imports.map { x =>
      val i = importer(x.split("\\.").toList)
      q"import ..$i"
    }
  }

  private def importer(imports: List[String]): List[Importer] = {
    val reversedImportNames = imports.init.map(Term.Name(_)).reverse
    val select =
      if (reversedImportNames.length > 1)
        recursiveTermSelect(reversedImportNames)
      else reversedImportNames.head

    if (imports.last == "_") {
      Importer(select, List(Importee.Wildcard())) :: Nil
    } else {
      Importer(select, List(Importee.Name(Name.Indeterminate(imports.last)))) :: Nil
    }
  }

  /**
    * Create a recursive Tree.Select structure for scalameta imports.
    * Note that the names must be in reversed order to build the correct tree.
    *
    * @param names to fold into a tree structure
    * @return names folded into a Term.Select from left to right
    */
  private def recursiveTermSelect(names: List[Term.Name]): Term.Select =
    names match {
      case Nil =>
        throw new IllegalStateException("Cannot create an empty import")
      case name :: Nil =>
        throw new IllegalStateException(
          s"Cannot create a import tree for a single name: $name")
      case name :: qual :: Nil => Term.Select(qual, name)
      case name :: qualifiers =>
        Term.Select(recursiveTermSelect(qualifiers), name)
    }
}
