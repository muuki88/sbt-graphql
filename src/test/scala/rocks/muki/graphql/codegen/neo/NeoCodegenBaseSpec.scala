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
package rocks.muki.graphql.codegen.neo


import org.scalatest.{EitherValues, TryValues, WordSpec}
import java.io.File

import rocks.muki.graphql.codegen.{Builder, Generator}

import scala.io
import scala.meta._

abstract class NeoCodegenBaseSpec(name: String, generator: (String => Generator[List[Stat]]))  extends WordSpec with EitherValues with TryValues {

  val inputDir = new File(s"src/test/resources/$name")

  def contentOf(file: File): String = io.Source.fromFile(file)(io.Codec.UTF8).mkString


  "Neo Sangria Codegen" should {
    for {
      input <- inputDir.listFiles()
      if input.getName.endsWith(".graphql")
      name = input.getName.replace(".graphql", "")
      expected = new File(inputDir, s"$name.scala")
      if expected.exists
    } {
      s"generate code for ${input.getName}" in {
        val builder = Builder(new File(inputDir, "schema.graphql"))

        val stats = builder
          .withQuery(input)
          .generate(generator(name))
          .right.value


        val actual = stats.map(_.show[Syntax]).mkString("\n")
        val expectedSource = contentOf(expected).parse[Source].get


        assert(actual === expectedSource.show[Syntax])
      }
    }
  }
}

