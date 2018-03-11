package rocks.muki.graphql

import rocks.muki.graphql.codegen._
import rocks.muki.graphql.schema.SchemaLoader
import sbt.Keys._
import sbt.{Result => _, _}

import scala.meta._

object GraphQLCodegenPlugin extends AutoPlugin {

  override def requires: Plugins = GraphQLPlugin

  object autoImport {
    val graphqlCodegenSchema = taskKey[File]("GraphQL schema file")
    val graphqlCodegenQueries = taskKey[Seq[File]]("GraphQL query documents")

    val graphqlCodegenStyle = settingKey[CodeGenStyles.Style]("The resulting code generation style")

    val graphqlCodegenPackage =
      settingKey[String]("Package for the generated code")
    val graphqlCodegen = taskKey[Seq[File]]("Generate GraphQL API code")


    val Apollo = CodeGenStyles.Apollo
    val Sangria = CodeGenStyles.Sangria

  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlCodegenStyle := Apollo,
    graphqlCodegenSchema := (resourceDirectory in Compile).value / "schema.graphql",
    resourceDirectories in graphqlCodegen := (resourceDirectories in Compile).value,
    includeFilter in graphqlCodegen := "*.graphql",
    excludeFilter in graphqlCodegen := HiddenFileFilter,
    graphqlCodegenQueries := Defaults
      .collectFiles(resourceDirectories in graphqlCodegen,
                    includeFilter in graphqlCodegen,
                    excludeFilter in graphqlCodegen)
      .value,
    sourceGenerators in Compile += graphqlCodegen.taskValue,
    graphqlCodegenPackage := "graphql.codegen",
    name in graphqlCodegen := "GraphQLCodegen",
    graphqlCodegen := {
      val log = streams.value.log
      val targetDir = sourceManaged.value / "sbt-graphql"
      //val generator = ScalametaGenerator((name in graphqlCodegen).value)
      val queries = graphqlCodegenQueries.value
      log.info(s"Generate code for ${queries.length} queries")
      log.info(s"Use schema ${graphqlCodegenSchema.value} for query validation")

      val packageName = graphqlCodegenPackage.value
      val schema =
        SchemaLoader.fromFile(graphqlCodegenSchema.value).loadSchema()

      val moduleName = (name in graphqlCodegen).value
      val context = CodeGenContext(schema, targetDir, queries, packageName, moduleName, log)

      graphqlCodegenStyle.value(context)
    }
  )

}
