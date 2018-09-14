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

  def termRefOf(typeTerm: String): Term.Ref = {
    typeTerm.parse[Term].get.asInstanceOf[Term.Ref]
  }
}
