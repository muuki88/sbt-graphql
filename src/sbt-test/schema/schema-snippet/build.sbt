name := "query-validation"

enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)

graphqlSchemaSnippet := "example.ProductSchema.schema"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "2.0.0-RC1",
  "org.sangria-graphql" %% "sangria-circe" % "1.3.0"
)
