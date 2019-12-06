package rocks.muki.graphql.codegen.style.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator, JsonCodeGens}

class ApolloForumCirceCodegenSpec
    extends ApolloCodegenBaseSpec(
      "forum-circe",
      (fileName: String) => ApolloSourceGenerator(fileName, Nil, GraphQLQueryGenerator.inits, JsonCodeGens.Circe)
    )
