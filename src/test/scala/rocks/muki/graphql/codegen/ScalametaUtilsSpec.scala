package rocks.muki.graphql.codegen

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.meta._

class ScalametaUtilsSpec extends AnyWordSpec with Matchers {

  "The ScalametaUtils" should {

    "generate valid imports" when {
      "a single member is imported" in {
        val imports = ScalametaUtils.imports("foo.bar.MyClass" :: Nil)
        imports should have size 1
        imports.head.show[Syntax] should be("import foo.bar.MyClass")
      }

      "a single member from a single package is imported" in {
        val imports = ScalametaUtils.imports("foo.MyClass" :: Nil)
        imports should have size 1
        imports.head.show[Syntax] should be("import foo.MyClass")
      }

      "a wildcard import is used" in {
        val imports = ScalametaUtils.imports("foo.bar._" :: Nil)
        imports should have size 1
        imports.head.show[Syntax] should be("import foo.bar._")
      }

      "a wildcard import from a single package is used" in {
        val imports = ScalametaUtils.imports("foo._" :: Nil)
        imports should have size 1
        imports.head.show[Syntax] should be("import foo._")
      }
    }
  }

}
