package rocks.muki.graphql.codegen

import scala.meta._

object ScalametaUtils {
  def typeRefOf(typeTerm: String): Type.Ref = {
    typeTerm.parse[Type].get.asInstanceOf[Type.Ref]
  }

  def termRefOf(typeTerm: String): Term.Ref = {
    typeTerm.parse[Term].get.asInstanceOf[Term.Ref]
  }
}
