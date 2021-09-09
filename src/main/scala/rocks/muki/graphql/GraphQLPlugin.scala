package rocks.muki.graphql

import sbt.Keys._
import sbt._
import rocks.muki.graphql.schema.{GraphQLSchemas, SchemaLoader}

/**
  * == GraphQL Plugin ==
  *
  * Root plugin for all other graphql plugins. Provides a schema registry that can be used for
  *
  * - validating queries against a specific schema
  * - comparing schemas
  * - code generation based on a specific schema
  *
  */
object GraphQLPlugin extends AutoPlugin {

  object autoImport {

    /**
      * Helper to load schemas from different places
      */
    val GraphQLSchemaLoader: SchemaLoader.type =
      rocks.muki.graphql.schema.SchemaLoader

    val GraphQLSchema: rocks.muki.graphql.schema.GraphQLSchema.type =
      rocks.muki.graphql.schema.GraphQLSchema

    /**
      * Contains all schemas available in this build.
      *
      * @example Adding a new schema
      * {{{
      * graphqlSchemas += GraphQLSchema(
      *   "temporary",
      *   "schema loaded from schema.json in the base directory",
      *   SchemaLoader.fromFile(baseDirectory.value / "schema.json")),
      * }}}
      *
      */
    val graphqlSchemas: SettingKey[GraphQLSchemas] =
      settingKey[GraphQLSchemas]("all schemas available in this build")

    /**
      * Renders the given schema into a graphql file.
      * The input is the label in the graphqlSchemas setting.
      */
    val graphqlRenderSchema: InputKey[File] =
      inputKey[File]("renders the given schema to a graphql file")

  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlSchemas := GraphQLSchemas(),
    // schema rendering
    graphqlRenderSchema / target := (Compile / target).value / "graphql",
    graphqlRenderSchema := graphqlRenderSchemaTask.evaluated
  )

  private val graphqlRenderSchemaTask = Def.inputTaskDyn[File] {
    val log = streams.value.log
    val schemaDefinition = singleGraphQLSchemaParser.parsed
    val file = (graphqlRenderSchema / target).value / s"${schemaDefinition.label}.graphql"
    log.info(s"Rendering schema to: ${file.getPath}")

    Def.task {
      val schema = schemaDefinition.schemaTask.value
      IO.write(file, schema.renderPretty)
      file
    }
  }

}
