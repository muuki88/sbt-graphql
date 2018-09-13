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

import cats.instances.string._
import cats.{Eq, Semigroup}
import sbt.io.IO

/**
  * A generic error type for codegen failures.
  */
case class Failure(message: String) extends Exception(message)

object Failure {

  /**
    * Useful to combine collections of failures (e.g. NonEmptyList) to one `Failure` using `.reduce`
    *
    * {{
    *   val failures: NonEmptyList[Failure] = ... // possibly from ValidatedNel
    *
    *   val combinedFailure: Failure = failures.reduce
    * }}
    *
    * Note: Because of the newline between the two values this doesn't satisfy the Monoid left- and right-identity
    *       laws and can only be a Semigroup (associativity law).
    */
  implicit val semigroupFailure: Semigroup[Failure] =
    (x: Failure, y: Failure) => Failure(x.message + IO.Newline + y.message)

  /**
    * Mainly used for law checking in tests
    */
  implicit val eqFailure: Eq[Failure] = Eq.by(_.message)
}
