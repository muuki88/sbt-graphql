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

import java.io.File

import cats.implicits._
import rocks.muki.graphql.instances.monoidDocument
import sangria.ast.Document
import sangria.parser.QueryParser
import sangria.schema._
import sangria.validation.QueryValidator

import scala.io.Source

object DocumentLoader {

  private val codeGenUseTypeArg: Argument[Option[String]] = Argument(
    "useType",
    OptionInputType(StringType),
    "Specify another type to be used"
  )

  private val codeGenDirective = Directive(
    name = "codeGen",
    description = Some("Directs the executor to include this fragment definition only when the `if` argument is true."),
    arguments = codeGenUseTypeArg :: Nil,
    locations = Set(
      DirectiveLocation.Field
    )
  )

  /**
    * Loads and parses all files and merge them into a single document
    * @param schema used to validate parsed files
    * @param files the files that should be loaded
    * @return
    */
  def merged(schema: Schema[Any, Any], files: List[File]): Result[Document] =
    /*_*/
    files
      .traverse(file => single(withDirectives(schema), file))
      .map(documents => documents.combineAll)
  /*_*/

  /**
    * Load a single, validated query file.
    * @param schema
    * @param file
    * @return
    */
  def single(schema: Schema[Any, Any], file: File): Result[Document] =
    for {
      document <- parseDocument(file)
      violations = QueryValidator.default.validateQuery(withDirectives(schema), document)
      _ <- Either.cond(
        violations.isEmpty,
        document,
        Failure(s"Invalid query in ${file.getAbsolutePath}:\n${violations.map(_.errorMessage).mkString(", ")}")
      )
    } yield document

  private def withDirectives(schema: Schema[Any, Any]): Schema[Any, Any] = schema.copy(
    directives = schema.directives :+ codeGenDirective
  )

  private def parseSchema(file: File): Result[Schema[_, _]] =
    for {
      document <- parseDocument(file)
      schema <- Either.catchNonFatal(Schema.buildFromAst(document)).leftMap { error =>
        Failure(s"Failed to read schema $file: ${error.getMessage}")
      }
    } yield schema

  private def parseDocument(file: File): Result[Document] =
    for {
      input <- Either.catchNonFatal(Source.fromFile(file).mkString).leftMap { error =>
        Failure(s"Failed to read $file: ${error.getMessage}")
      }
      document <- Either.fromTry(QueryParser.parse(input)).leftMap { error =>
        Failure(s"Failed to parse $file: ${error.getMessage}")
      }
    } yield document
}
