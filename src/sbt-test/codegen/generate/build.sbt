name := "test"
enablePlugins(GraphQLCodegenPlugin)
scalaVersion := "2.11.11"

TaskKey[Unit]("check") := {
  val file = (graphqlCodegen in Compile).value
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
  assert(IO.read(file).trim == expected)
}
