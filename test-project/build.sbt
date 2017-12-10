name := "graphql-test-project"

version := "0.4"

enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)

graphqlSchemaSnippet := "example.StarWarsSchema.schema"

graphqlSchemas += GraphQLSchema(
  "sangria-example",
  "staging schema at http://try.sangria-graphql.org/graphql",
  Def.task(
    GraphQLSchemaLoader
      .fromIntrospection("http://try.sangria-graphql.org/graphql", streams.value.log)
      .withHeaders("User-Agent" -> "sbt-graphql/${version.value}")
      .loadSchema()
  ).taskValue
)

addCommandAlias("validateSangriaExample", "graphqlValidateSchema build sangria-example")
