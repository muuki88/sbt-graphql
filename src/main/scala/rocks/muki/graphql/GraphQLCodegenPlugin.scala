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
    val graphqlCodegenPackage =
      settingKey[String]("Package for the generated code")
    val graphqlCodegen = taskKey[File]("Generate GraphQL API code")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlCodegenSchema := (resourceDirectory in Compile).value / "schema.graphql",
    resourceDirectories in graphqlCodegen := (resourceDirectories in Compile).value,
    includeFilter in graphqlCodegen := "*.graphql",
    excludeFilter in graphqlCodegen := HiddenFileFilter,
    graphqlCodegenQueries := Defaults
      .collectFiles(resourceDirectories in graphqlCodegen,
		    includeFilter in graphqlCodegen,
		    excludeFilter in graphqlCodegen)
      .value,
    sourceGenerators in Compile += Def.task { Seq(graphqlCodegen.value) },
    graphqlCodegenPackage := "graphql.codegen",
    name in graphqlCodegen := "GraphQLCodegen",
    graphqlCodegen := {
      val log = streams.value.log
      val output = sourceManaged.value / "sbt-graphql" / "GraphQLCodegen.scala"
      val generator = ScalametaGenerator((name in graphqlCodegen).value)
      val queries = graphqlCodegenQueries.value
      log.info(s"Generate code for ${queries.length} queries")
      val packageName = graphqlCodegenPackage.value
      val schema = graphqlCodegenSchema.value
      log.info(s"Use schema $schema for query validation")

      val builder =
	if (schema.getName.endsWith(".json"))
	  Builder(SchemaLoader.fromFile(schema).loadSchema())
	else
	  Builder(schema)

      val result = builder
	.withQuery(queries: _*)
	.generate(generator)
	.map { code =>
	  IO.createDirectory(output.getParentFile)
	  IO.writeLines(output,
			List(s"package $packageName", code.show[Syntax]))
	}

      result match {
	case Left(error) => sys.error(s"Failed to generate code: $error")
	case Right(()) => output
      }
    }
  )

}
