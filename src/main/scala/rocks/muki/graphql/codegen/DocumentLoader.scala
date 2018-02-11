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
import scala.io.Source
import cats.syntax.either._
import sangria.parser.QueryParser
import sangria.validation.QueryValidator
import sangria.schema._
import sangria.ast.Document


object DocumentLoader {

  /**
    * Loads and parses all files and merge them into a single document
    * @param schema used to validate parsed files
    * @param files the files that should be loaded
    * @return
    */
  def merged(schema: Schema[_, _], files: List[File]): Result[Document] = {
    files.map(single(schema, _)).foldLeft[Result[Document]](Right(Document.emptyStub)) {
      case (Left(failure), Left(nextFailure)) => Left(Failure(failure.message + "\n" + nextFailure.message))
      case (Left(failure), _) => Left(failure)
      case (_, Left(firstFailure) ) => Left(firstFailure)
      case (Right(document), Right(nextDocument)) => Right(document.merge(nextDocument))
    }
  }

  /**
    * Load a single, validated query file.
    * @param schema
    * @param file
    * @return
    */
  def single(schema: Schema[_, _], file: File): Result[Document] = {
    for {
      document <- parseDocument(file)
      violations = QueryValidator.default.validateQuery(schema, document)
      _ <- Either.cond(violations.isEmpty, document, Failure(s"Invalid query: ${violations.map(_.errorMessage).mkString(", ")}"))
    } yield document
  }

  private def parseSchema(file: File): Result[Schema[_, _]] =
    for {
      document <- parseDocument(file)
      schema <- Either.catchNonFatal(Schema.buildFromAst(document)).leftMap {
        error =>
          Failure(s"Failed to read schema $file: ${error.getMessage}")
      }
    } yield schema

  private def parseDocument(file: File): Result[Document] =
    for {
      input <- Either.catchNonFatal(Source.fromFile(file).mkString).leftMap {
        error =>
          Failure(s"Failed to read $file: ${error.getMessage}")
      }
      document <- Either.fromTry(QueryParser.parse(input)).leftMap { error =>
        Failure(s"Failed to parse $file: ${error.getMessage}")
      }
    } yield document
}
