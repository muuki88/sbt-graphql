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
    val graphqlCodegenJson =
      taskKey[JsonCodeGen]("Configure a json decoder code generator")

    val graphqlCodegenStyle =
      settingKey[CodeGenStyles.Style]("The resulting code generation style")

    val graphqlCodegenPackage =
      settingKey[String]("Package for the generated code")

    val graphqlCodegenImports =
      settingKey[Seq[String]]("Additional imports to add to the generated code")

    val graphqlCodegen = taskKey[Seq[File]]("Generate GraphQL API code")

    val Apollo = CodeGenStyles.Apollo
    val Sangria = CodeGenStyles.Sangria

    val JsonCodec = JsonCodeGens

  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlCodegenStyle := Apollo,
    graphqlCodegenSchema := (resourceDirectory in Compile).value / "schema.graphql",
    graphqlCodegenJson := JsonCodeGens.None,
    sourceDirectory in graphqlCodegen := (sourceDirectory in Compile).value / "graphql",
    sourceDirectories in graphqlCodegen := List(
      (sourceDirectory in graphqlCodegen).value),
    includeFilter in graphqlCodegen := "*.graphql",
    excludeFilter in graphqlCodegen := HiddenFileFilter,
    graphqlCodegenQueries := Defaults
      .collectFiles(sourceDirectories in graphqlCodegen,
                    includeFilter in graphqlCodegen,
                    excludeFilter in graphqlCodegen)
      .value,
    sourceGenerators in Compile += graphqlCodegen.taskValue,
    graphqlCodegenPackage := "graphql.codegen",
    graphqlCodegenImports := Seq.empty,
    name in graphqlCodegen := "GraphQLCodegen",
    graphqlCodegen := {
      val log = streams.value.log
      val targetDir = sourceManaged.value / "sbt-graphql"
      //val generator = ScalametaGenerator((name in graphqlCodegen).value)
      val queries = graphqlCodegenQueries.value
      log.info(s"Generate code for ${queries.length} queries")
      log.info(
        s"Use schema ${graphqlCodegenSchema.value} for query validation")

      val packageName = graphqlCodegenPackage.value
      val schema =
        SchemaLoader.fromFile(graphqlCodegenSchema.value).loadSchema()

      val imports = graphqlCodegenImports.value

      val jsonCodeGen = graphqlCodegenJson.value
      log.info(
        s"Generating json decoding with: ${jsonCodeGen.getClass.getSimpleName}")

      log.info(
        s"Adding imports: ${imports.mkString(",")}")

      val moduleName = (name in graphqlCodegen).value
      val context = CodeGenContext(schema,
                                   targetDir,
                                   queries,
                                   packageName,
                                   moduleName,
                                   jsonCodeGen,
                                   imports,
                                   log)

      graphqlCodegenStyle.value(context)
    }
  )

}
