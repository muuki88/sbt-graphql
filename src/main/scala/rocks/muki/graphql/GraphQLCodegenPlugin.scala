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
        graphqlCodegen / sourceDirectory := sourceDirectory.value / "graphql",
        graphqlCodegen / sourceDirectories := List((config / graphqlCodegen / sourceDirectory).value),
        graphqlCodegen / target := sourceManaged.value / "sbt-graphql",
        graphqlCodegenQueries := Defaults
          .collectFiles(
            graphqlCodegen / sourceDirectories,
            graphqlCodegen / includeFilter,
            graphqlCodegen / excludeFilter
          )
          .value,
        config / graphqlCodegenPreProcessors := List(
          PreProcessors.magicImports((config / graphqlCodegen / sourceDirectories).value)
        ),
        config / graphqlCodegen := {
          val log = streams.value.log
          val targetDir = (config / graphqlCodegen / target).value
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

          val moduleName = (config / graphqlCodegen / name).value
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
      graphqlCodegenSchema := (Compile / resourceDirectory).value / "schema.graphql",
      graphqlCodegenJson := JsonCodeGens.None,
      graphqlCodegen / includeFilter := "*.graphql",
      graphqlCodegen / excludeFilter := HiddenFileFilter || "*.fragment.graphql",
      graphqlCodegenPackage := "graphql.codegen",
      graphqlCodegenImports := Seq.empty,
      graphqlCodegen / name := "GraphQLCodegen",
      graphqlCodegen := (Compile / graphqlCodegen).value
    ) ++ codegenTask(Compile) ++ codegenTask(Test)

}
