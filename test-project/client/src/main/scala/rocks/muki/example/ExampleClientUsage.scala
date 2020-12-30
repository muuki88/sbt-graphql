package rocks.muki.example

import cats.syntax.option._
import rocks.muki.example.client.GraphQLClient
import rocks.muki.example.client.GraphQLClient.GraphQLResult
import rocks.muki.graphql.films._

/**
  * Run with:
  *   sbt "client/runMain rocks.muki.example.ExampleClientUsage"
  */
object ExampleClientUsage extends App {

  // Demo server located at https://graphql.org/swapi-graphql
  val graphqlServerUrl = "https://swapi-graphql.netlify.app/.netlify/functions/index"

  val result: GraphQLResult[FilmsQuery.Data] =
    GraphQLClient.sendQuery(graphqlServerUrl, FilmsQuery)(FilmsQuery.Variables(first = 3).some)

  println(s"\nGraphQLResult:\n\n$result\n")
}
