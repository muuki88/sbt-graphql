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

import org.scalatest.{EitherValues, Matchers, WordSpec}
import java.io.File
import sangria.parser.QueryParser

class BuilderSpec extends WordSpec with Matchers with EitherValues {
  import starwars.TestSchema.StarWarsSchema

  val generator = ScalametaGenerator("CodegenResult")

  "Builder" should {
    "fail with non-existent schema" in {
      val result = Builder(new File("schema-file-does-not-exist"))
	.generate[Tree.Api]

      result.left.value.message should startWith(
	"Failed to read schema-file-does-not-exist: schema-file-does-not-exist")

    }

    "fail with non-existent query" in {
      val result = Builder(StarWarsSchema)
	.withQuery(new File("query-file-does-not-exist"))
	.generate[Tree.Api]

      result.left.value.message should startWith(
	"Failed to read query-file-does-not-exist: query-file-does-not-exist")
    }

    "validate query documents" in {
      val scala.util.Success(query) =
	QueryParser.parse("""
	query HeroName($episdoe: Episode!) {
	  hero(episode: $episode) {
	    name
	  }
	}
      """)
      val expectedMessage =
	"""Invalid query: Variable '$episode' is not defined by operation 'HeroName'. (line 3, column 25):
	  |          hero(episode: $episode) {
	  |                        ^
	  | (line 2, column 9):
	  |        query HeroName($episdoe: Episode!) {
	  |        ^, Variable '$episdoe' is not used in operation HeroName. (line 2, column 24):
	  |        query HeroName($episdoe: Episode!) {
	  |                       ^""".stripMargin
      val Left(failure) =
	Builder(StarWarsSchema).withQuery(query).generate[Tree.Api]

      failure should be(Failure(expectedMessage))
    }

    "merge query documents" in {
      val tree = Builder(StarWarsSchema)
	.withQuery(
	  new File("src/test/resources/starwars/HeroAndFriends.graphql"))
	.withQuery(
	  new File("src/test/resources/starwars/HeroNameQuery.graphql"))
	.generate[Tree.Api]
	.right
	.value

      val names = tree.operations.flatMap(_.name)
      names should contain only ("HeroAndFriends", "HeroNameQuery")

    }
  }
}
