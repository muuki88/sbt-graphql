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

package rocks.muki.graphql.codegen.sangria

import java.io.File

import org.scalatest.{EitherValues, WordSpec}
import rocks.muki.graphql.codegen.{
  DocumentLoader,
  ScalametaGenerator,
  TypedDocumentParser
}
import rocks.muki.graphql.schema.SchemaLoader
import sangria.schema.Schema
import sbt._

import scala.io.Source
import scala.meta._

abstract class SangriaCodegenBaseSpec(name: String,
                                      schema: Option[Schema[_, _]] = None)
    extends WordSpec
    with EitherValues {
  def this(name: String, schema: Schema[_, _]) = this(name, Some(schema))

  val inputDir = new File("src/test/resources/sangria", name)

  def contentOf(file: File) =
    Source.fromFile(file).mkString

  "SangriaCodegen" should {
    for {
      input <- inputDir.listFiles()
      if input.getName.endsWith(".graphql")
      name = input.getName.replace(".graphql", "")
      expected = new File(inputDir, s"$name.scala")
      if expected.exists
    } {
      s"generate code for ${input.getName}" in {
        val generator = ScalametaGenerator(s"${name}Api")
        val schema =
          SchemaLoader
            .fromFile(inputDir / "schema.graphql")
            .loadSchema()

        val document = DocumentLoader.single(schema, input).right.value
        val typedDocument =
          TypedDocumentParser(schema, document).parse().right.value
        val out = generator(typedDocument).right.value

        val actual = out.show[Syntax]

        if (actual.trim != contentOf(expected).trim)
          println(actual)

        assert(actual.trim == contentOf(expected).trim)
      }
    }
  }
}
