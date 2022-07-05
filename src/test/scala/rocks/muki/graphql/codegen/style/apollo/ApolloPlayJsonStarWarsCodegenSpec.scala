package rocks.muki.graphql.codegen.style.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator, JsonCodeGens}

class ApolloPlayJsonStarWarsCodegenSpec
    extends ApolloCodegenBaseSpec(
      "starwars-play-json",
      (fileName: String) => ApolloSourceGenerator(fileName, Nil, GraphQLQueryGenerator.inits, JsonCodeGens.PlayJson)
    )
