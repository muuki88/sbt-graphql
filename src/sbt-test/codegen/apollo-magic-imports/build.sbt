name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0"
)

graphqlCodegenStyle := Apollo

TaskKey[Unit]("check") := {
  val generatedFiles = (graphqlCodegen in Compile).value
  val interfacesFile = generatedFiles.find(_.getName == "Interfaces.scala")

  assert(interfacesFile.isDefined, s"Could not find generated scala class. Available files\n  ${generatedFiles.mkString("\n  ")}")
}
