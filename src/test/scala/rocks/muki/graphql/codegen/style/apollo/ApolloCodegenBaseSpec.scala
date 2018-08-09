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
package rocks.muki.graphql.codegen.style.apollo

import org.scalatest.{EitherValues, TryValues, WordSpec}
import java.io.File

import rocks.muki.graphql.codegen.{
  ApolloSourceGenerator,
  DocumentLoader,
  TypedDocumentParser
}
import rocks.muki.graphql.schema.SchemaLoader

import scala.io.{Codec, Source => IOSource}
import scala.meta._
import sbt._

abstract class ApolloCodegenBaseSpec(
    name: String,
    generator: String => ApolloSourceGenerator)
    extends WordSpec
    with EitherValues
    with TryValues {

  val inputDir = new File(s"src/test/resources/apollo", name)

  def contentOf(file: File): String =
    IOSource.fromFile(file)(Codec.UTF8).mkString

  "Apollo Sangria Codegen" should {
    for {
      input <- inputDir.listFiles()
      if input.getName.endsWith(".graphql")
      name = input.getName.replace(".graphql", "")
      expected = new File(inputDir, s"$name.scala")
      if expected.exists
    } {
      s"generate code for ${input.getName}" in {

        val schema =
          SchemaLoader
            .fromFile(inputDir / "schema.graphql")
            .loadSchema()
        val document = DocumentLoader.single(schema, input).right.value
        val typedDocument =
          TypedDocumentParser(schema, document).parse().right.value
        val stats = generator(input.getName)(typedDocument).right.value

        val actual = stats.map(_.show[Syntax]).mkString("\n")
        val expectedSource = contentOf(expected).parse[Source].get

        assert(actual === expectedSource.show[Syntax].trim,
               s"------\n$actual\n------")
      }
    }

    for {
      input <- inputDir.listFiles()
      if input.getName.endsWith(".graphql")
      name = input.getName.replace(".graphql", "")
      expected = new File(inputDir, s"${name}Interfaces.scala")
      if expected.exists
    } {
      s"generate interface code for ${input.getName}" in {

        val schema =
          SchemaLoader
            .fromFile(inputDir / "schema.graphql")
            .loadSchema()
        val document = DocumentLoader.single(schema, input).right.value
        val typedDocument =
          TypedDocumentParser(schema, document).parse().right.value
        val stats = generator(input.getName)
          .generateInterfaces(typedDocument)
          .right
          .value

        val actual = stats.map(_.show[Syntax]).mkString("\n")
        val expectedSource = contentOf(expected).parse[Source].get

        assert(actual === expectedSource.show[Syntax].trim, s"------\n$actual\n------")
      }
    }

    for {
      input <- inputDir.listFiles()
      if input.getName.endsWith(".graphql")
      name = input.getName.replace(".graphql", "")
      expected = new File(inputDir, s"${name}Types.scala")
      if expected.exists
    } {
      s"generate types code for ${input.getName}" in {

        val schema =
          SchemaLoader
            .fromFile(inputDir / "schema.graphql")
            .loadSchema()
        val document = DocumentLoader.single(schema, input).right.value
        val typedDocument =
          TypedDocumentParser(schema, document).parse().right.value
        val stats = generator(input.getName)
          .generateTypes(typedDocument)
          .right
          .value

        val actual = stats.map(_.show[Syntax]).mkString("\n")
        val expectedSource = contentOf(expected).parse[Source].get

        assert(actual === expectedSource.show[Syntax].trim, s"------\n$actual\n------")
      }
    }

  }

}
