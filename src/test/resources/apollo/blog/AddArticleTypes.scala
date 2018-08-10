object types {
  case class ArticleAuthor(id: ID)
  case class ArticleContent(title: String, body: String, tags: Option[List[String]], author: ArticleAuthor)
  type ID = String
}