package rocks.muki.graphql.schema

import java.io.File

import io.circe.Json
import io.circe.parser.parse
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
  def fromFile(file: File): Schema[Any, Any] = new FileSchemaLoader(file).loadSchema()

  /**
   * Loads a schema from an graphql endpoint.
   *
   * @param url the graphql endpoint
   * @param log log output
   */
  def fromIntrospection(url: String, log: Logger): Schema[Any, Any] = new IntrospectSchemaLoader(url, log).loadSchema()

}

/**
 * Loads a schema from an schema json file.
 *
 * @param file the schema json file
 */
class FileSchemaLoader(file: File) extends SchemaLoader {

  override def loadSchema(): Schema[Any, Any] = {
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
class IntrospectSchemaLoader(url: String, log: Logger) extends SchemaLoader {

  override def loadSchema(): Schema[Any, Any] = Schema.buildFromIntrospection(introspect)

  /**
   * @see https://github.com/graphql/graphql-js/blob/master/src/utilities/introspectionQuery.js
   * @return the introspect query result
   */
  private def introspect(): Json = {
    log.info(s"Introspect graphql endpoint: $url")
    val introspectQuery = gql"""
     query IntrospectionQuery {
         __schema {
           queryType { name }
           mutationType { name }
           subscriptionType { name }
           types {
             ...FullType
           }
           directives {
             name
             description
             locations
             args {
               ...InputValue
             }
           }
         }
       }
       fragment FullType on __Type {
         kind
         name
         description
         fields(includeDeprecated: true) {
           name
           description
           args {
             ...InputValue
           }
           type {
             ...TypeRef
           }
           isDeprecated
           deprecationReason
         }
         inputFields {
           ...InputValue
         }
         interfaces {
           ...TypeRef
         }
         enumValues(includeDeprecated: true) {
           name
           description
           isDeprecated
           deprecationReason
         }
         possibleTypes {
           ...TypeRef
         }
       }
       fragment InputValue on __InputValue {
         name
         description
         type { ...TypeRef }
         defaultValue
       }
       fragment TypeRef on __Type {
         kind
         name
         ofType {
           kind
           name
           ofType {
             kind
             name
             ofType {
               kind
               name
               ofType {
                 kind
                 name
                 ofType {
                   kind
                   name
                   ofType {
                     kind
                     name
                     ofType {
                       kind
                       name
                     }
                   }
                 }
               }
             }
           }
         }
       }"""
    val response = Http(url).param("query", introspectQuery.renderCompact).asString
    parse(response.body) match {
      case Right(json) => json
      case Left(error) =>
        log.error("JSON parse errors:")
        log.error(error.message)
        sys.error(s"Invalid json was returned from graphql endpoint $url")
    }
  }
}