package rocks.muki.graphql.schema

import java.io.File

import io.circe.Json
import io.circe.parser.parse
import sangria.introspection.introspectionQuery
import sangria.macros._
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import sangria.schema.Schema
import sbt.io.IO
import sbt.util.Logger

import scala.util.{Failure, Success}
import scalaj.http.Http

/**
  * Defines a specific way for loading graphql schemas.
  */
trait SchemaLoader {

  /**
    * Load the schema.
    * The Context and Val types are always any as we cannot make any
    * assumptions while loading an external schema.
    *
    * @return the successful loaded schema
    */
  def loadSchema(): Schema[Any, Any]
}

object SchemaLoader {

  /**
    * Loads a schema from an schema json file.
    *
    * @param file the schema json file
    */
  def fromFile(file: File): FileSchemaLoader =
    new FileSchemaLoader(file)

  /**
    * Loads a schema from an graphql endpoint.
    *
    * @param url the graphql endpoint
    * @param log log output
    */
  def fromIntrospection(url: String, log: Logger): IntrospectSchemaLoader =
    IntrospectSchemaLoader(url, log)

}

/**
  * Loads a schema from an schema json file.
  *
  * @param file the schema json file
  */
class FileSchemaLoader(file: File) extends SchemaLoader {

  override def loadSchema(): Schema[Any, Any] = {
    //  TODO check if it's a json or graphql file and parse accordingly
    val schemaJson = IO.read(file)
    QueryParser.parse(schemaJson) match {
      case Success(document) => Schema.buildFromAst(document)
      case Failure(error) => throw error
    }
  }
}

/**
  * Loads a schema from an graphql endpoint.
  *
  * @param url the graphql endpoint
  * @param log log output
  */
case class IntrospectSchemaLoader(url: String,
                                  log: Logger,
                                  headers: Seq[(String, String)] = Seq.empty)
    extends SchemaLoader {

  override def loadSchema(): Schema[Any, Any] =
    Schema.buildFromIntrospection(introspect())

  def withHeaders(headers: (String, String)*): IntrospectSchemaLoader = {
    copy(headers = headers.toList)
  }

  /**
    * @see https://github.com/graphql/graphql-js/blob/master/src/utilities/introspectionQuery.js
    * @return the introspect query result
    */
  private def introspect(): Json = {
    log.info(s"Introspect graphql endpoint: $url")
    val response = Http(url)
      .headers(headers)
      .param("query", introspectionQuery.renderCompact)
      .asString
    parse(response.body) match {
      case Right(json) => json
      case Left(error) =>
        log.error("JSON parse errors:")
        log.error(error.message)
        sys.error(s"Invalid JSON was returned from graphql endpoint $url")
    }
  }
}
