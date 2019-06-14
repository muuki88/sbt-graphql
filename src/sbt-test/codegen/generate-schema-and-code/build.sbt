import scala.sys.process._

name := "test"
scalaVersion in ThisBuild := "2.12.4"

val server = project
  .enablePlugins(GraphQLSchemaPlugin)
  .settings(
    libraryDependencies += "org.sangria-graphql" %% "sangria" % "1.4.2",
    graphqlSchemaSnippet :=
      "com.example.starwars.TestSchema.StarWarsSchema"
  )

val client = project
  .enablePlugins(GraphQLCodegenPlugin)
  .settings(
    graphqlCodegenStyle := Sangria,
    graphqlCodegenSchema := (graphqlSchemaGen in server).value,
    graphqlCodegenPackage := "com.example.client.api",
    name in graphqlCodegen := "MultiQueryApi"
  )

TaskKey[Unit]("check") := {
  val files  = (graphqlCodegen in client).value

  assert(files.length == 1, s"Sangria code should only generated one file, but got ${files.length}.\n${files.mkString("\n")}")

  val file = files.head
  assert(file.exists, s"$file could not be found")

  val content = IO.read(file)
  assert(content.contains("object MultiQueryApi"), s"MultiQueryApi not presented in generated code\n${content}")
}
