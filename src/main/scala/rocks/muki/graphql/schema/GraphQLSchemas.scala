package rocks.muki.graphql.schema

import sbt._
import sangria.schema.Schema

/**
  *
  * @param label unique and human readable label for this schema
  * @param description for this schema
  * @param schemaTask the task generating this schema
  */
case class GraphQLSchema(label: String, description: String, schemaTask: Task[Schema[Any, Any]])

/**
  * == GraphQL Schemas ==
  *
  * Data structure for storing all schemas defined in the build.
  *
  * @param schemas a vector of all defined schemas
  */
case class GraphQLSchemas(schemas: Vector[GraphQLSchema] = Vector.empty) {

  val schemaByLabel: Map[String, GraphQLSchema] =
    schemas.groupBy(_.label).mapValues(_.head)

  override def toString: String =
    schemas
      .map { schema =>
        s"""|## Label: ${schema.label}
        |  Description: ${schema.description}
     """.stripMargin
      }
      .mkString("\n")
}

object GraphQLSchemas {

  /**
    * type class for `++=` syntax in sbt files
    */
  implicit val appendValuesVector: Append.Values[GraphQLSchemas, Vector[GraphQLSchema]] =
    (gqlSchemas: GraphQLSchemas, schemas: Vector[GraphQLSchema]) =>
      gqlSchemas.copy(schemas = gqlSchemas.schemas ++ schemas)

  /**
    * type class for `++=` syntax in sbt files
    */
  implicit val appendValuesSeq: Append.Values[GraphQLSchemas, Seq[GraphQLSchema]] =
    (gqlSchemas: GraphQLSchemas, schemas: Seq[GraphQLSchema]) =>
      gqlSchemas.copy(schemas = gqlSchemas.schemas ++ schemas)

  /**
    * type class for `+=` syntax in sbt files
    */
  implicit val appendValue: Append.Value[GraphQLSchemas, GraphQLSchema] =
    (gqlSchemas: GraphQLSchemas, schema: GraphQLSchema) => gqlSchemas.copy(schemas = gqlSchemas.schemas :+ schema)

}
