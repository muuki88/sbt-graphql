package rocks.muki.graphql.codegen.style.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator, JsonCodeGens}

class ApolloAnimalsCirceCodegenSpec
    extends ApolloCodegenBaseSpec(
      "animals-circe",
      (fileName: String) => ApolloSourceGenerator(fileName, Nil, GraphQLQueryGenerator.inits, JsonCodeGens.Circe)
    )
