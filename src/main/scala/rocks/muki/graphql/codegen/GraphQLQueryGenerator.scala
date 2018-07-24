package rocks.muki.graphql.codegen

import scala.meta._

/**
  * Provides a `GraphQLQuery` trait. This is heavily inspired by the apollo scalajs code generator.
  *
  *
  * {{{
  *   trait GraphQLQuery {
  *      // the graphql document that should be executed
  *      type Document
  *
  *      // the input variables
  *      type Variables
  *
  *      // the returned data
  *      type Data
  *   }
  * }}}
  */
object GraphQLQueryGenerator {

  val name = "GraphQLQuery"
  val termName: Term.Name = Term.Name(name)
  val typeName: Type.Name = Type.Name(name)

  private val traitDefinition: Defn.Trait =
    q"""trait $typeName {
          type Document
          type Variables
          type Data
          val document: Document
        }
     """

  /**
    * Generates the actual source code.
    *
    * @param packageName the package in which the GraphQLQuery trait should be rendered
    * @return the GraphQLTrait source code
    */
  def sourceCode(packageName: String): Pkg =
    q"""package ${Term.Name(packageName)} {
       $traitDefinition
     }"""

  /**
    * Add these imports to your generated code.
    *
    * @param packageName the GraphQLQuery package
    * @return
    */
  def imports(packageName: String): List[Import] = {
    val importer =
      Importer(Term.Name(packageName), List(Importee.Name(Name(name))))
    List(
      q"import ..${List(importer)}"
    )
  }

  /**
    * Scala meta `Init` definitions. Use these to extend a generated class with the
    * GraphQLQuery trait.
    */
  val inits: List[Init] = List(
    Init(GraphQLQueryGenerator.typeName, Name.Anonymous(), Nil)
  )

}
