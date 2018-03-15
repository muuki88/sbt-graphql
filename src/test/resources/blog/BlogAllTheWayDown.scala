object BlogAllTheWayDownApi {
  case class Blog(blog: BlogAllTheWayDownApi.Blog.Blog)
  object Blog {
    case class BlogVariables(blogId: BlogAllTheWayDownApi.ID)
    case class Blog(title: String)
  }
  type ID = String
}
