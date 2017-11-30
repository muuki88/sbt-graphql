import scala.sys.process._

name := "test"
scalaVersion in ThisBuild := "2.12.3"

val StarWarsDir = file(sys.props("codegen.samples.dir")) / "starwars"

val server = project
  .enablePlugins(GraphQLSchemaPlugin)
  .settings(
    libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.3.2",
    graphqlSchemaSnippet :=
      "com.example.starwars.TestSchema.StarWarsSchema"
  )

val client = project
  .enablePlugins(GraphQLCodegenPlugin)
  .settings(
    graphqlCodegenSchema := (graphqlSchemaGen in server).value,
    resourceDirectories in graphqlCodegen += StarWarsDir,
    includeFilter in graphqlCodegen := "MultiQuery.graphql",
    graphqlCodegenPackage := "com.example.client.api",
    name in graphqlCodegen := "MultiQueryApi"
  )

TaskKey[Unit]("check") := {
  val file     = (graphqlCodegen in client).value
  val expected = StarWarsDir / "MultiQuery.scala"

  assert(file.exists)

  // Drop the package line before comparing
  val compare = IO.readLines(file).drop(1).mkString("\n").trim == IO.read(expected).trim
  if (!compare)
    s"diff -u $expected $file".!
  assert(compare, s"$file does not equal $expected")
}
