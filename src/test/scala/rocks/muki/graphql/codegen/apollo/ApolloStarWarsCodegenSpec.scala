package rocks.muki.graphql.codegen.apollo

import rocks.muki.graphql.codegen.{ApolloSourceGenerator, GraphQLQueryGenerator}


class ApolloStarWarsCodegenSpec extends ApolloCodegenBaseSpec("apollo/starwars",
  (fileName: String) => ApolloSourceGenerator(fileName, GraphQLQueryGenerator.imports("com.example"), GraphQLQueryGenerator.inits))