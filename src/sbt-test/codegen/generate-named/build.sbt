import scala.sys.process._

name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.12.4"

val StarWarsDir = file(sys.props("codegen.samples.dir")) / "starwars"

graphqlCodegenSchema := StarWarsDir / "schema.graphql"
resourceDirectories in graphqlCodegen += StarWarsDir
includeFilter in graphqlCodegen := "MultiQuery.graphql"
name in graphqlCodegen := "MultiQueryApi"

TaskKey[Unit]("check") := {
  val file     = graphqlCodegen.value
  val expected = StarWarsDir / "MultiQuery.scala"

  assert(file.exists)

  // Drop the package line before comparing
  val compare = IO.readLines(file).drop(1).mkString("\n").trim == IO.read(expected).trim
  if (!compare)
    s"diff -u $expected $file".!
  assert(compare, s"$file does not equal $expected")
}
