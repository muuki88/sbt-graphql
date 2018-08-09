object types {
  case class ArticleQuery(ids: Option[List[ID]], statuses: Option[List[ArticleStatus]])
  sealed trait ArticleStatus
  object ArticleStatus {
    case object DRAFT extends ArticleStatus
    case object PUBLISHED extends ArticleStatus
  }
  type ID = String
}