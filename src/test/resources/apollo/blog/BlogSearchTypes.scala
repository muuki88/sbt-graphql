object types {
  sealed trait ArticleStatus
  object ArticleStatus {
    case object DRAFT extends ArticleStatus
    case object PUBLISHED extends ArticleStatus
  }
  case class Pagination(first: Int, count: Int, order: Option[PaginationOrder])
  sealed trait PaginationOrder
  object PaginationOrder {
    case object ASC extends PaginationOrder
    case object DESC extends PaginationOrder
  }
  type ID = String
}