package rocks.muki

package object graphql {

  def quietError(msg: String): Nothing = {
    val exc = new RuntimeException(msg)
    exc.setStackTrace(Array.empty)
    throw exc
  }
}
