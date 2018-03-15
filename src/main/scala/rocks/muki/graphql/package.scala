package rocks.muki

import rocks.muki.graphql.GraphQLPlugin.autoImport.graphqlSchemas
import rocks.muki.graphql.schema.{GraphQLSchema, GraphQLSchemas}
import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.{FixedSetExamples, Parser}

package object graphql {

  /**
    * Throw an exception without a stacktrace.
    *
    * @param msg the error message
    * @return nothing - throws an exception
    */
  def quietError(msg: String): Nothing = {
    val exc = new RuntimeException(msg)
    exc.setStackTrace(Array.empty)
    throw exc
  }

  /**
    * @return a parser that parses exactly one schema l
    */
  val singleGraphQLSchemaParser: Def.Initialize[Parser[GraphQLSchema]] =
    Def.setting {
      val gqlSchema = graphqlSchemas.value
      val labels = gqlSchema.schemas.map(_.label)
      // create a dependent parser. A label can only be selected once
      schemaLabelParser(labels).map(label => schemaOrError(label, gqlSchema))
    }

  /**
    * Parses two schema labels
    */
  val tupleGraphQLSchemaParser
    : Def.Initialize[Parser[(GraphQLSchema, GraphQLSchema)]] =
    Def.setting {
      val gqlSchemas = graphqlSchemas.value
      val labels = gqlSchemas.schemas.map(_.label)
      // create a depended parser. A label can only be selected once
      schemaLabelParser(labels).flatMap {
        case selectedLabel if labels.contains(selectedLabel) =>
          success(schemaOrError(selectedLabel, gqlSchemas)) ~ schemaLabelParser(
            labels.filterNot(_ == selectedLabel)).map(label =>
            schemaOrError(label, gqlSchemas))
        case selectedLabel =>
          failure(
            s"$selectedLabel is not available. Use: [${labels.mkString(" | ")}]")
      }
    }

  /**
    * @param labels list of available schemas by label
    * @return a parser for the given labels
    */
  private[this] def schemaLabelParser(
      labels: Iterable[String]): Parser[String] = {
    val schemaParser = StringBasic.examples(FixedSetExamples(labels))
    token(Space.? ~> schemaParser)
  }

  private def schemaOrError(label: String,
                            graphQLSchema: GraphQLSchemas): GraphQLSchema =
    graphQLSchema.schemaByLabel.getOrElse(
      label,
      sys.error(s"The schema '$label' is not defined in graphqlSchemas"))

}
