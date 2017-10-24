name := "graphql-test-project"

enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)

graphqlSchemaSnippet := "example.ProductSchema.schema"

graphqlSchemas += GraphQLSchema(
  "sangria-example",
  "staging schema at http://try.sangria-graphql.org/graphql",
  Def.task(
    GraphQLSchemaLoader
      .fromIntrospection("http://try.sangria-graphql.org/graphql", streams.value.log)
      .loadSchema()
  ).taskValue
)