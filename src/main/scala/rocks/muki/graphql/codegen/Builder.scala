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

case class Builder private (
    schema: Result[Schema[_, _]],
    document: Result[Document] = Builder.emptyDocumentResult
) {
  private def withQuery(query: => Result[Document]): Builder = {
    val validatedQuery = schema.flatMap { validSchema =>
      query.flatMap { loadedQuery =>
        val violations =
          QueryValidator.default.validateQuery(validSchema, loadedQuery)
        if (violations.isEmpty)
          query
        else
          Left(Failure(
            s"Invalid query: ${violations.map(_.errorMessage).mkString(", ")}"))
      }
    }

    if (document == Builder.emptyDocumentResult)
      copy(document = validatedQuery)
    else
      copy(document = document.flatMap(doc => validatedQuery.map(doc.merge)))
  }

  def withQuery(query: Document): Builder =
    withQuery(Right(query))

  def withQuery(queryFiles: File*): Builder =
    queryFiles.foldLeft(this) {
      case (builder, file) =>
        builder.withQuery(Builder.parseDocument(file))
    }

  def generate[T](implicit generator: Generator[T]): Result[T] =
    for {
      validSchema <- schema
      validDocument <- document
      api <- Importer(validSchema, validDocument).parse
      result <- generator(api)
    } yield result
}

object Builder {
  def apply(schema: Schema[_, _]): Builder = new Builder(Right(schema))
  def apply(schemaFile: File): Builder = new Builder(parseSchema(schemaFile))

  private val emptyDocumentResult: Result[Document] = Right(Document.emptyStub)

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
