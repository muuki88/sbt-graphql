package rocks.muki.graphql

import rocks.muki.graphql.schema.SchemaLoader
import sangria.ast.Document
import sangria.schema._
import sbt._
import sbt.Keys._

object GraphQLSchemaPlugin extends AutoPlugin {

  // The main class for the schema generator class
  private val mainClass = "SchemaGen"
  // The package for the schema generated class
  private val packageName = "graphql"

  object autoImport {
    /**
     * A scala snippet that returns the [[sangria.schema.Schema]] for your graphql application.
     *
     * @example if your schema is defined on an object
     * {{{
     *   graphqlSchemaSnippet := "com.example.MySchema.schema"
     * }}}
     */
    val graphqlSchemaSnippet: SettingKey[String] = settingKey[String]("code snippet that returns the sangria Schema")

    /**
     * Generates a the graphql schema based on the code snippet provided via `graphqlSchemaSnippet`
     */
    val graphqlSchemaGen: TaskKey[File] = taskKey[File]("generates a graphql schema file")

    /**
     * Returns the changes between the schema from `graphqlSchemaGen` and
     */
    val graphqlSchemaChanges: TaskKey[Vector[SchemaChange]] = taskKey[Vector[SchemaChange]]("compares two schemas")

    /**
     * The currently active / deployed graphql schema.
     */
    val graphqlProductionSchema: TaskKey[Schema[Any,Any]] = taskKey[Schema[Any,Any]]("Graphql schema from your production system")

    /**
     * Validates the new schema against existing queries and the production schema
     */
    val graphqlValidateSchema: TaskKey[Unit] = taskKey[Unit]("Validates the new schema against existing queries and the production schema")

    /**
     * Creates realease notes for changes between the production and the current schema
     */
    val graphqlReleaseNotes: TaskKey[String] = taskKey[String]("Creates realease notes for changes between the production and the current schema")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlSchemaSnippet := """sys.error("Configure the `graphqlSchemaSnippet` setting with the correct scala code snippet to access your sangria schema")""",
    graphqlProductionSchema := Schema.buildFromAst(Document.emptyStub),
    graphqlSchemaChanges := SchemaLoader.fromFile(graphqlSchemaGen.value) compare graphqlProductionSchema.value,
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

    graphqlValidateSchema := {
      val log = streams.value.log
      val breakingChanges = graphqlSchemaChanges.value.filter(_.breakingChange)
      if(breakingChanges.nonEmpty) {
        breakingChanges.foreach(change => log.error(s" * ${change.description}"))
        quietError("Validation failed: Breaking changes found")
      }
    },

    graphqlReleaseNotes := graphqlSchemaChanges.value
      .map(change => s"* ${change.description}")
      .mkString(start = "## Changes\n", sep = "\n", end = "\n"),


    // Generates a small snippet that generates a graphql schema
    sourceGenerators in Compile += generateSchemaGeneratorClass()
  )

  /**
   * Generates a small code snippet that accesses the schema definition in the original
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

}
