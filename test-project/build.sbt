lazy val root = project
  .in(file("."))
  .aggregate(server, client)

lazy val server = project
  .in(file("server"))
  .enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)
  .settings(commonSettings)
  .settings(
    graphqlSchemaSnippet := "example.StarWarsSchema.schema"
  )
  .settings(
    addCommandAlias("validateStarWars", "graphqlValidateSchema build starwars")
  )

lazy val client = project
  .in(file("client"))
  .enablePlugins(GraphQLCodegenPlugin, GraphQLQueryPlugin)
  .settings(commonSettings)
  .settings(
    graphqlCodegenStyle := Apollo,
    graphqlCodegenJson := JsonCodec.Circe,
    graphqlCodegenSchema := graphqlRenderSchema.toTask("starwars-local").value,
    graphqlCodegenImports ++= List("java.time._"),
    graphqlCodegenPackage := "rocks.muki.graphql",
    graphqlSchemas += GraphQLSchema(
      "starwars-local",
      "starwars schema at server/src/main/resources",
      Def
        .task(
          GraphQLSchemaLoader
            .fromFile((resourceDirectory in (server, Compile)).value / "schema.graphql")
            .loadSchema()
        )
        .taskValue
    ),
    name in graphqlCodegen := "Api",
    // includeFilter in graphqlCodegen := "product.graphql"
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % "0.9.3")
  )

lazy val commonSettings = Seq(
  version := "0.4",
  scalaVersion := "2.12.4",
  organization := "rocks.muki",
  libraryDependencies ++= Seq(
    "org.sangria-graphql" %% "sangria" % "2.0.0-RC1",
    "org.sangria-graphql" %% "sangria-circe" % "1.3.0"
  ),
  // define schemas available in all builds
  graphqlSchemas += GraphQLSchema(
    "starwars",
    "starwars schema at http://try.sangria-graphql.org/graphql",
    Def
      .task(
        GraphQLSchemaLoader
          .fromIntrospection("http://try.sangria-graphql.org/graphql", streams.value.log)
          .withHeaders("User-Agent" -> s"sbt-graphql/${version.value}")
          .loadSchema()
      )
      .taskValue
  )
)
