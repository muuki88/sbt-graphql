package rocks.muki.graphql.codegen

import cats.kernel.laws.discipline.SemigroupTests
import cats.tests.CatsSuite
import org.scalacheck.Arbitrary

class FailureInstancesSpec extends CatsSuite {
  implicit private val failureArb: Arbitrary[Failure] = Arbitrary(
    Arbitrary.arbString.arbitrary.map(Failure(_)))

  checkAll("Semigroup[Failure]", SemigroupTests[Failure].semigroup)
}
