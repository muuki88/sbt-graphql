package rocks.muki.graphql

import sangria.ast.Document
import sangria.schema._
import sbt._
import sbt.Keys._
import complete.{FixedSetExamples, Parser}
import complete.DefaultParsers._
import rocks.muki.graphql.schema.{GraphQLSchemas, SchemaLoader}

object GraphQLSchemaPlugin extends AutoPlugin {

  // The main class for the schema generator class
  private val mainClass = "SchemaGen"
  // The package for the schema generated class
  private val packageName = "graphql"

  object autoImport {

    /**
      * Helper to load schemas from different places
      */
    val GraphQLSchemaLoader: SchemaLoader.type =
      rocks.muki.graphql.schema.SchemaLoader

    val GraphQLSchema: rocks.muki.graphql.schema.GraphQLSchema.type =
      rocks.muki.graphql.schema.GraphQLSchema

    object GraphQLSchemaLabels {

      /**
        * Label for the schema generated by the project build
        */
      val BUILD: String = "build"

      /**
        * Label for the production schema
        */
      val PROD: String = "prod"
    }

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
      * A scala snippet that returns the [[sangria.schema.Schema]] for your graphql application.
      *
      * @example if your schema is defined on an object
      * {{{
      *   graphqlSchemaSnippet := "com.example.MySchema.schema"
      * }}}
      */
    val graphqlSchemaSnippet: SettingKey[String] =
      settingKey[String]("code snippet that returns the sangria Schema")

    /**
      * Generates a the graphql schema based on the code snippet provided via `graphqlSchemaSnippet`
      */
    val graphqlSchemaGen: TaskKey[File] =
      taskKey[File]("generates a graphql schema file")

    /**
      * Returns the changes between the two schemas defined as parameters.
      *
      * `graphqlSchemaChanges <new schema> <old schema>`
      *
      * @example compare two schemas
      * {{{
      * $ sbt
      * > graphqlSchemaChanges build prod
      * }}}
      *
      */
    val graphqlSchemaChanges: InputKey[Vector[SchemaChange]] =
      inputKey[Vector[SchemaChange]]("compares two schemas")

    /**
      * The currently active / deployed graphql schema.
      */
    val graphqlProductionSchema: TaskKey[Schema[Any, Any]] =
      taskKey[Schema[Any, Any]]("Graphql schema from your production system")

    /**
      * Validates the new schema against existing queries and the production schema
      */
    val graphqlValidateSchema: InputKey[Unit] = inputKey[Unit](
      "Validates the new schema against existing queries and the production schema")

    /**
      * Creates release notes for changes between the production and the current schema
      */
    val graphqlReleaseNotes: InputKey[String] = inputKey[String](
      "Creates release notes for changes between the production and the current schema")

  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlSchemaSnippet := """sys.error("Configure the `graphqlSchemaSnippet` setting with the correct scala code snippet to access your sangria schema")""",
    graphqlProductionSchema := Schema.buildFromAst(Document.emptyStub),
    graphqlSchemaChanges := graphqlSchemaChangesTask.evaluated,
    graphqlSchemaGen := {
      val schemaFile = resourceManaged.value / "sbt-sangria-codegen" / "schema.graphql"
      runner.value.run(
        s"$packageName.$mainClass",
        Attributed.data((fullClasspath in Compile).value),
        List(schemaFile.getAbsolutePath),
        streams.value.log
      )
      streams.value.log.info(s"Generating schema in $schemaFile")
      schemaFile
    },
    graphqlSchemas := GraphQLSchemas(),
    graphqlSchemas += GraphQLSchema(
      GraphQLSchemaLabels.BUILD,
      "schema generated by this build (graphqlSchemaGen task)",
      graphqlSchemaGen.map(SchemaLoader.fromFile(_).loadSchema()).taskValue
    ),
    graphqlSchemas += GraphQLSchema(
      GraphQLSchemaLabels.PROD,
      "schema generated by the graphqlProductionSchema task",
      graphqlProductionSchema.taskValue),
    graphqlValidateSchema := graphqlValidateSchemaTask.evaluated,
    graphqlReleaseNotes := graphqlSchemaChanges.evaluated
      .map(change => s"* ${change.description}")
      .mkString(start = "## Changes\n", sep = "\n", end = "\n"),
    // Generates a small snippet that generates a graphql schema
    sourceGenerators in Compile += generateSchemaGeneratorClass(),
    graphqlSchemas += GraphQLSchema(
      "staging",
      "staging schema at staging.your-graphql.net/graphql",
      Def
        .task(
          GraphQLSchemaLoader
            .fromIntrospection("http://staging.your-graphql.net/graphql",
                               streams.value.log)
            .loadSchema()
        )
        .taskValue
    )
  )

  /**
    * Generates a small code snippet that accessres the schema definition in the original
    * code base and renders it as a graphql schema definition file.
    *
    * @see https://github.com/mediative/sangria-codegen/blob/master/sbt-sangria-codegen/src/main/scala/com.mediative.sangria.codegen.sbt/SangriaSchemagenPlugin.scala#L121-L153
    * @return
    */
  private def generateSchemaGeneratorClass() = Def.task {
    val schemaCode = graphqlSchemaSnippet.value
    val file = sourceManaged.value / "sbt-sangria-codegen" / s"$mainClass.scala"

    val content = s"""|package $packageName
                      |object $mainClass {
                      |  val schema: sangria.schema.Schema[_, _] = {
                      |    $schemaCode
                      |  }
                      |  def main(args: Array[String]): Unit = {
                      |    val schemaFile = new java.io.File(args(0))
                      |    val graphql: String = schema.renderPretty
                      |    schemaFile.getParentFile.mkdirs()
                      |    new java.io.PrintWriter(schemaFile) {
                      |      write(graphql)
                      |      close
                      |    }
                      |    ()
                      |  }
                      |}
      """.stripMargin

    IO.write(file, content)
    Seq(file)
  }

  /**
    * @param labels list of available schemas by label
    * @return a parser for the given labels
    */
  private def schemaLabelParser(labels: Iterable[String]): Parser[String] = {
    val schemaParser = StringBasic.examples(FixedSetExamples(labels))
    token(Space ~> schemaParser)
  }

  /**
    * Parses two schema labels
    */
  private val graphqlSchemaChangesParser
    : Def.Initialize[Parser[(String, String)]] = Def.setting {
    val labels = graphqlSchemas.value.schemas.map(_.label)
    // create a depened parser. A label can only be selected once
    schemaLabelParser(labels).flatMap { selectedLabel =>
      success(selectedLabel) ~ schemaLabelParser(
        labels.filterNot(_ == selectedLabel))
    }
  }

  private val graphqlSchemaChangesTask = Def.inputTaskDyn {
    val log = streams.value.log
    val (oldSchemaLabel, newSchemaLabel) = graphqlSchemaChangesParser.parsed

    val schemas = graphqlSchemas.value.schemaByLabel
    Def.task {
      val newSchema = schemas(newSchemaLabel).schemaTask.value
      val oldSchema = schemas(oldSchemaLabel).schemaTask.value
      log.info(s"Comparing $oldSchemaLabel with $newSchemaLabel schema")
      oldSchema compare newSchema
    }
  }

  private val graphqlValidateSchemaTask = Def.inputTask[Unit] {
    val log = streams.value.log
    val breakingChanges =
      graphqlSchemaChanges.evaluated.filter(_.breakingChange)
    if (breakingChanges.nonEmpty) {
      breakingChanges.foreach(change => log.error(s" * ${change.description}"))
      quietError("Validation failed: Breaking changes found")
    }
  }

}
