name := "graphql-test-project"

enablePlugins(GraphQLSchemaPlugin)

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)

graphqlSchemaSnippet := "example.ProductSchema.schema"