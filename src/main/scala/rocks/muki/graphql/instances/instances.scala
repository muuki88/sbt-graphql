package rocks.muki.graphql

import cats.Monoid
import sangria.ast.Document

package object instances {

  /**
    * Useful if you want to combine documents using `.combineAll`, `.foldMap` or `.foldMapM`.
    *
    * Note that there is also `Documents.merge`, which has nothing to do with cats, but merge multiple Documents as well.
    */
  implicit val monoidDocument: Monoid[Document] = new Monoid[Document] {
    override def empty: Document = Document(Vector.empty)
    override def combine(x: Document, y: Document): Document = x merge y
  }
}
