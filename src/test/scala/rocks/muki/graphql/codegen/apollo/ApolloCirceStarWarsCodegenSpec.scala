package rocks.muki.graphql.codegen.apollo

import rocks.muki.graphql.codegen.{
  ApolloSourceGenerator,
  GraphQLQueryGenerator,
  JsonCodeGens
}

class ApolloCirceStarWarsCodegenSpec
    extends ApolloCodegenBaseSpec(
      "starwars-circe",
      (fileName: String) =>
        ApolloSourceGenerator(fileName,
                              Nil,
                              GraphQLQueryGenerator.inits,
                              JsonCodeGens.Circe)
    )
