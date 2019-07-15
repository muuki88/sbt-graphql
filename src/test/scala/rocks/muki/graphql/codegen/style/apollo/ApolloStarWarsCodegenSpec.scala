package rocks.muki.graphql.codegen.style.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator, JsonCodeGens}

class ApolloStarWarsCodegenSpec
    extends ApolloCodegenBaseSpec(
      "starwars",
      (fileName: String) => ApolloSourceGenerator(fileName, Nil, GraphQLQueryGenerator.inits, JsonCodeGens.None)
    )
