name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.12.4"

graphqlCodegenStyle := Sangria

TaskKey[Unit]("check") := {
  val file = (graphqlCodegen in Compile).value.head
  val expected =
    """package graphql.codegen
      |
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
  assert(generated == expected, s"Generated file:\n$generated")
}
