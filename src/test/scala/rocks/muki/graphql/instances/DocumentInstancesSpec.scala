package rocks.muki.graphql.instances

import cats._
import cats.kernel.laws.discipline.MonoidTests
import cats.tests.CatsSuite
import org.scalacheck.{Arbitrary, Gen}
import rocks.muki.graphql.codegen.style.sangria.TestSchema
import sangria.ast.Document

class DocumentInstancesSpec extends CatsSuite {
  implicit private val arbDocument: Arbitrary[Document] =
    Arbitrary(
      Gen.oneOf(
        Document(Vector.empty),
        Document.emptyStub,
        Document(TestSchema.StarWarsSchema.toAst.definitions.take(3))
      )
    )
  implicit private val eqDocument: Eq[Document] =
    Eq.fromUniversalEquals[Document]

  checkAll("Monoid[sangria.ast.Document]", MonoidTests[Document].monoid)
}
