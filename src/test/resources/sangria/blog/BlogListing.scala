object BlogListingApi {
  case class BlogListing(blogs: List[BlogListingApi.BlogListing.Blogs])
  object BlogListing {
    case class BlogListingVariables(pagination: Pagination)
    case class Blogs(id: BlogListingApi.ID, title: String, uri: String)
  }
  case class Pagination(first: Int, count: Int, order: Option[PaginationOrder])
  sealed trait PaginationOrder
  object PaginationOrder {
    case object ASC extends BlogListingApi.PaginationOrder
    case object DESC extends BlogListingApi.PaginationOrder
  }
  type ID = String
}
