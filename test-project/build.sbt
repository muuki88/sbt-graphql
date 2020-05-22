Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / turbo := true

lazy val root = project
  .in(file("."))
  .aggregate(server, client)

/**
  * This is an example project where sangria schema definitions (in Scala code) are validated against a
  * schema (local file or external server).
  *
  * Run `validateStarWarsLocal` or `validateStarWarsExternal` to see how the validation errors look like.
  */
lazy val server = project
  .in(file("server"))
  .enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)
  .settings(commonSettings)
  .settings(
    // schema defined as sangria-code to validate (= check that your sangria-server code matches an existing schema)
    graphqlSchemaSnippet := "example.StarWarsSchema.schema",
    // schema to validate against (can be a local file or pulled from an external graphql-server via introspection)
    graphqlSchemas ++= List(
      GraphQLSchema(
        "starwars-local",
        "starwars schema at server/src/main/resources",
        Def
          .task(
            GraphQLSchemaLoader
              .fromFile((resourceDirectory in Compile).value / "schema.graphql")
              .loadSchema()
          )
          .taskValue
      ),
      GraphQLSchema(
        "starwars-external",
        "starwars schema at https://graphql.org/swapi-graphql/",
        Def
          .task(
            GraphQLSchemaLoader
              .fromIntrospection("https://swapi-graphql.netlify.app/.netlify/functions/index", streams.value.log)
              .withHeaders("User-Agent" -> s"sbt-graphql/${version.value}")
              .loadSchema()
          )
          .taskValue
      )
    )
  )
  .settings(
    addCommandAlias("validateStarWarsLocal", "graphqlValidateSchema build starwars-local"),
    addCommandAlias("validateStarWarsExternal", "graphqlValidateSchema build starwars-external")
  )

/**
  * This is an example project where client-code is generated from the external schema and the client-side defined
  * queries.
  *
  * Run `runMain rocks.muki.example.ExampleClientUsage` to send a query and see the response.
  */
lazy val client = project
  .in(file("client"))
  .enablePlugins(GraphQLCodegenPlugin, GraphQLQueryPlugin)
  .settings(commonSettings)
  .settings(
    graphqlCodegenStyle := Apollo,
    graphqlCodegenJson := JsonCodec.Circe,
    graphqlCodegenSchema := graphqlRenderSchema.toTask("starwars-external").value,
    // example of how to import custom types (e.g. if your schema had a custom `java.time.Instant` type which is encoded as unix epoch millis)
    graphqlCodegenImports ++= List(
      "java.time._",
      "rocks.muki.example.codecs._"
    ),
    // where to put generated code
    graphqlCodegenPackage := "rocks.muki.graphql",
    graphqlSchemas += GraphQLSchema(
      "starwars-external",
      "starwars schema at https://graphql.org/swapi-graphql/",
      Def
        .task(
          GraphQLSchemaLoader
            .fromIntrospection("https://swapi-graphql.netlify.app/.netlify/functions/index", streams.value.log)
            .withHeaders("User-Agent" -> s"sbt-graphql/${version.value}")
            .loadSchema()
        )
        .taskValue
    ),
    name in graphqlCodegen := "Api",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % "0.13.0"),
    // only needed for example client implementation (rocks.muki.example.client.GraphQLClient)
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "requests" % "0.5.1",
      "org.typelevel" %% "cats-core" % "2.2.0-M1"
    )
  )

lazy val commonSettings = Seq(
  version := "0.4",
  scalaVersion := "2.12.11",
  organization := "rocks.muki",
  libraryDependencies ++= Seq(
    "org.sangria-graphql" %% "sangria" % "2.0.0-RC1",
    "org.sangria-graphql" %% "sangria-circe" % "1.3.0"
  )
)
