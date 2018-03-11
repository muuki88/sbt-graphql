import scala.sys.process._

name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.12.4"

graphqlCodegenStyle := Sangria

TaskKey[Unit]("check") := {
  val file = (graphqlCodegen in Compile).value.head
  val expected =
    """package graphql.codegen
      |object GraphQLCodegen {
      |  case class HeroNameQuery(hero: GraphQLCodegen.HeroNameQuery.Hero)
      |  object HeroNameQuery {
      |    case class HeroNameQueryVariables()
      |    case class Hero(name: Option[String])
      |  }
      |}
    """.stripMargin.trim

  assert(file.exists)
  val generated = IO.read(file).trim

  // Drop the package line before comparing
  val compare = IO.readLines(file).drop(1).mkString("\n").trim == expected.trim
  if (!compare) {
    IO.withTemporaryDirectory { dir =>
      val expectedFile = dir / "expected.scala"
      IO.write(expectedFile, expected)
      s"diff -u $expectedFile $file".!

    }
  }

  assert(generated == expected, s"Generated file:\n$generated")
}
