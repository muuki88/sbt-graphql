package rocks.muki.graphql.codegen.style.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator, JsonCodeGens}

class ApolloBlogCodegenSpec
    extends ApolloCodegenBaseSpec(
      "blog",
      (fileName: String) => ApolloSourceGenerator(fileName, Nil, GraphQLQueryGenerator.inits, JsonCodeGens.None)
    )
