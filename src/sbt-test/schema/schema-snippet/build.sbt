name := "query-validation"

enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)

graphqlSchemaSnippet := "example.ProductSchema.schema"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)
