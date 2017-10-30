name := "query-validation"

enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)

graphqlSchemaSnippet := "example.ProductSchema.schema"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)

graphqlSchemas += GraphQLSchema(
  "product-schema",
  "fixed schema at schemas/product.graphql",
  Def.task(
    GraphQLSchemaLoader
      .fromFile(baseDirectory.value / "schemas" / "product.graphql")
      .loadSchema()
  ).taskValue
)

graphqlSchemas += GraphQLSchema(
  "product-schema-broken",
  "fixed schema at schemas/product-broken.graphql",
  Def.task(
    GraphQLSchemaLoader
      .fromFile(baseDirectory.value / "schemas" / "product-broken.graphql")
      .loadSchema()
  ).taskValue
)