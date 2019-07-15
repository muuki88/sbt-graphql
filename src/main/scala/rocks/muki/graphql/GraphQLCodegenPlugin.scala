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

    val graphqlCodegenPreProcessors =
      taskKey[Seq[PreProcessor]]("Preprocessors that should be applied before the graphql file is parsed")

    val graphqlCodegen = taskKey[Seq[File]]("Generate GraphQL API code")

    val Apollo = CodeGenStyles.Apollo
    val Sangria = CodeGenStyles.Sangria

    val JsonCodec = JsonCodeGens

  }
  import autoImport._

  def codegenTask(config: Configuration) =
    inConfig(config)(
      Seq(
        sourceGenerators += graphqlCodegen.taskValue,
        sourceDirectory in graphqlCodegen := sourceDirectory.value / "graphql",
        sourceDirectories in graphqlCodegen := List((sourceDirectory in (config, graphqlCodegen)).value),
        target in graphqlCodegen := sourceManaged.value / "sbt-graphql",
        graphqlCodegenQueries := Defaults
          .collectFiles(
            sourceDirectories in graphqlCodegen,
            includeFilter in graphqlCodegen,
            excludeFilter in graphqlCodegen
          )
          .value,
        graphqlCodegenPreProcessors in config := List(
          PreProcessors.magicImports((sourceDirectories in (config, graphqlCodegen)).value)
        ),
        graphqlCodegen in config := {
          val log = streams.value.log
          val targetDir = (target in (config, graphqlCodegen)).value
          //val generator = ScalametaGenerator((name in graphqlCodegen).value)
          val queries = graphqlCodegenQueries.value
          val schemaFile = graphqlCodegenSchema.value
          log.info(s"Generate code for ${queries.length} queries")
          log.info(s"Use schema $schemaFile for query validation")

          val packageName = graphqlCodegenPackage.value
          val schema =
            SchemaLoader.fromFile(schemaFile).loadSchema()

          val imports = graphqlCodegenImports.value
          val jsonCodeGen = graphqlCodegenJson.value
          val preProcessors = graphqlCodegenPreProcessors.value
          log.info(s"Generating json decoding with: ${jsonCodeGen.getClass.getSimpleName}")

          log.info(s"Adding imports: ${imports.mkString(",")}")

          val moduleName = (name in (config, graphqlCodegen)).value
          val context = CodeGenContext(
            schema,
            targetDir,
            queries,
            packageName,
            moduleName,
            jsonCodeGen,
            imports,
            preProcessors,
            log
          )

          graphqlCodegenStyle.value(context)
        }
      )
    )

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      graphqlCodegenStyle := Apollo,
      graphqlCodegenSchema := (resourceDirectory in Compile).value / "schema.graphql",
      graphqlCodegenJson := JsonCodeGens.None,
      includeFilter in graphqlCodegen := "*.graphql",
      excludeFilter in graphqlCodegen := HiddenFileFilter || "*.fragment.graphql",
      graphqlCodegenPackage := "graphql.codegen",
      graphqlCodegenImports := Seq.empty,
      name in graphqlCodegen := "GraphQLCodegen",
      graphqlCodegen := (graphqlCodegen in Compile).value
    ) ++ codegenTask(Compile) ++ codegenTask(Test)

}
