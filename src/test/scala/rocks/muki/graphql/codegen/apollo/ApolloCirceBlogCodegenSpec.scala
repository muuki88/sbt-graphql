package rocks.muki.graphql.codegen.apollo

import rocks.muki.graphql.codegen.{
  ApolloSourceGenerator,
  GraphQLQueryGenerator,
  JsonCodeGens
}

class ApolloCirceBlogCodegenSpec
    extends ApolloCodegenBaseSpec(
      "blog-circe",
      (fileName: String) =>
        ApolloSourceGenerator(fileName,
                              GraphQLQueryGenerator.imports("com.example"),
                              GraphQLQueryGenerator.inits,
                              JsonCodeGens.Circe)
    )
