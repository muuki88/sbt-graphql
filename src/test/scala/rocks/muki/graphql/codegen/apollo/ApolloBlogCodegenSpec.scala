package rocks.muki.graphql.codegen.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator}

class ApolloBlogCodegenSpec
    extends ApolloCodegenBaseSpec(
      "apollo/blog",
      (fileName: String) =>
        ApolloSourceGenerator(fileName,
                              GraphQLQueryGenerator.imports("com.example"),
                              GraphQLQueryGenerator.inits))
