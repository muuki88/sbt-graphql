package rocks.muki.graphql.codegen

import scala.meta._

/**
  * More robust way to parse [[Type.Ref]] and [[Term.Ref]] from String.
  * More discussion: https://gitter.im/scalameta/scalameta?at=5b9ba14f8909f71f75d1b4bd
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
}
