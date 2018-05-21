import scala.sys.process._

name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.12.4"

graphqlCodegenStyle := Sangria

graphqlCodegenSchema := baseDirectory.value / "schema.graphql"
includeFilter in graphqlCodegen := "MultiQuery.graphql"
name in graphqlCodegen := "MultiQueryApi"


TaskKey[Unit]("check") := {
  val files  = graphqlCodegen.value

  assert(files.length == 1, s"Sangria code should only generated one file, but got ${files.length}.\n${files.mkString("\n")}")

  val file = files.head
  assert(file.exists, s"$file could not be found")

  val content = IO.read(file)
  assert(content.contains("object MultiQueryApi"), s"MultiQueryApi not presented in generated code\n${content}")
}
