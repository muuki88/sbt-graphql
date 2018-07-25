import sangria.macros._
object BlogByID {
  object Blog extends GraphQLQuery {
    val document: sangria.ast.Document = graphql"""query Blog($$blogId: ID!) {
  blog(id: $$blogId) {
    title
  }
}"""
    case class Variables(blogId: ID)
    case class Data(blog: Blog)
    case class Blog(title: String)
  }
  type ID = String
}
