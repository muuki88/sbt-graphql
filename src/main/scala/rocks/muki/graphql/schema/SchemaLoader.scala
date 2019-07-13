package rocks.muki.graphql.schema

import java.io.File

import io.circe.Json
import io.circe.jackson.parse
import sangria.introspection.introspectionQuery
import sangria.macros._
import rocks.muki.graphql.instances._
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
    // check if it's a json or graphql file and parse accordingly
    if (file.getName.endsWith(".json")) {
      parse(IO.read(file)) match {
        case Right(schemaJson) =>
          Schema.buildFromIntrospection(schemaJson)
        case Left(parsingFailure) =>
          throw parsingFailure
      }
    } else {
      val schemaGraphql = IO.read(file)
      QueryParser.parse(schemaGraphql) match {
        case Success(document) => Schema.buildFromAst(document)
        case Failure(error) => throw error
      }
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
                                  headers: Seq[(String, String)] = Seq.empty,
                                  method: IntrospectSchemaLoader.Method =
                                    IntrospectSchemaLoader.GET)
    extends SchemaLoader {

  override def loadSchema(): Schema[Any, Any] =
    Schema.buildFromIntrospection(introspect())

  def withHeaders(headers: (String, String)*): IntrospectSchemaLoader = {
    copy(headers = headers.toList)
  }

  /**
    * @return a new schema loader that uses a POST requests instead of a get request
    */
  def withPost(): IntrospectSchemaLoader = {
    copy(method = IntrospectSchemaLoader.POST)
  }

  /**
    * @see https://github.com/graphql/graphql-js/blob/master/src/utilities/introspectionQuery.js
    * @return the introspect query result
    */
  private def introspect(): Json = {
    log.info(s"Introspect graphql endpoint: ${method.name} : $url")

    val response = method match {
      case IntrospectSchemaLoader.POST =>
        val body = Json
          .obj("query" -> Json.fromString(introspectionQuery.renderCompact))
          .noSpaces
        Http(url).headers(headers).method("POST").postData(body).asString
      case IntrospectSchemaLoader.GET =>
        Http(url)
          .headers(headers)
          .param("query", introspectionQuery.renderCompact)
          .asString
    }

    parse(response.body) match {
      case Right(json) => json
      case Left(error) =>
        log.error("JSON parse errors:")
        log.error(error.message)
        log.error("Body received")
        log.error(response.body)
        sys.error(
          s"Invalid JSON was returned from graphql endpoint ${method.name} : $url")
    }
  }
}

object IntrospectSchemaLoader {

  /**
    * http method for introspection query
    */
  sealed abstract class Method(val name: String)
  case object POST extends Method("POST")
  case object GET extends Method("GET")
}
