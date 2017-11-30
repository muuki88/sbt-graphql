package rocks.muki.graphql

import sangria.ast.Document
import sangria.schema._
import sbt.{ Result => _, _ }
import sbt.Keys._
import complete.{FixedSetExamples, Parser}
import complete.DefaultParsers._
import rocks.muki.graphql.codegen._
import cats.syntax.either._
import _root_.io.circe.Json
import java.io.PrintStream
import scala.meta.{io => _, _}
import sangria.marshalling.circe._

object GraphQLCodegenPlugin extends AutoPlugin {

  object autoImport {
    val graphqlCodegenSchema  = taskKey[File]("GraphQL schema file")
    val graphqlCodegenQueries = taskKey[Seq[File]]("GraphQL query documents")
    val graphqlCodegenPackage = settingKey[String]("Package for the generated code")
    val graphqlCodegen        = taskKey[File]("Generate GraphQL API code")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    graphqlCodegenSchema := (resourceDirectory in Compile).value / "schema.graphql",
    resourceDirectories in graphqlCodegen := (resourceDirectories in Compile).value,
    includeFilter in graphqlCodegen := "*.graphql",
    excludeFilter in graphqlCodegen := HiddenFileFilter,
    graphqlCodegenQueries := Defaults
      .collectFiles(
	resourceDirectories in graphqlCodegen,
	includeFilter in graphqlCodegen,
	excludeFilter in graphqlCodegen)
      .value,
    sourceGenerators in Compile += Def.task { Seq(graphqlCodegen.value) },
    graphqlCodegenPackage := "graphql.codegen",
    name in graphqlCodegen := "GraphQLCodegen",
    graphqlCodegen := {
      val output = sourceManaged.value / "sbt-graphql" / "GraphQLCodegen.scala"
      val generator = ScalametaGenerator((name in graphqlCodegen).value)
      val queries = graphqlCodegenQueries.value
      val packageName = graphqlCodegenPackage.value
      val schema = graphqlCodegenSchema.value
      val builder =
	if (schema.getName.endsWith(".json"))
	  Builder(parseIntrospectionSchemaFile(schema))
	else
	  Builder(schema)

      val result = builder
	.withQuery(queries: _*)
	.generate(generator)
	.map { code =>
	  output.getParentFile.mkdirs()
	  val stdout = new PrintStream(output)
	  stdout.println(s"package $packageName")
	  stdout.println()
	  stdout.println(code.show[Syntax])
	  stdout.close()
	}

      result match {
	case Left(error) => sys.error("Failed to generate code: $error")
	case Right(()) => output
      }
    }
  )

  def parseIntrospectionSchemaFile(schemaFile: File): Result[Schema[_, _]] =
    _root_.io.circe.jawn
      .parseFile(schemaFile)
      .flatMap(parseIntrospectionSchemaJson)
      .leftMap { error: Throwable =>
	Failure(s"Failed to parse schema in $schemaFile: ${error.getMessage}")
      }

  def parseIntrospectionSchemaJson(json: Json): Either[Throwable, Schema[_, _]] =
    Either.catchNonFatal {
      val builder = new DefaultIntrospectionSchemaBuilder[Unit]
      Schema.buildFromIntrospection[Unit, Json](json, builder)
    }

  def parseHeaders(headers: List[String]): List[(String, String)] =
    headers.filter(_.nonEmpty).map { header =>
      header.split("=", 2) match {
	case Array(name, value) => name -> value
	case Array(name)        => name -> ""
      }
    }
}
