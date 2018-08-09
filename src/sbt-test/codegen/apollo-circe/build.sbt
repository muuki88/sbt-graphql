name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.12.4"

graphqlCodegenStyle := Apollo

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ %  "0.9.3")

TaskKey[Unit]("check") := {
  val generatedFiles = (graphqlCodegen in Compile).value
  assert(generatedFiles.length == 6, s"Expected 6 files to be generated, but got\n${generatedFiles.mkString("\n")}")
}
