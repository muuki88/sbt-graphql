package rocks.muki.graphql.codegen.style.apollo

import rocks.muki.graphql.codegen.{
  ApolloSourceGenerator,
  GraphQLQueryGenerator,
  JsonCodeGens
}
import scala.meta._

class ApolloImportsStarWarsCodegenSpec
    extends ApolloCodegenBaseSpec(
      "starwars-imports",
      (fileName: String) =>
        ApolloSourceGenerator(fileName,
                              List(q"import java.time._"),
                              GraphQLQueryGenerator.inits,
                              JsonCodeGens.None)
    )
