package rocks.muki.graphql

import cats.Monoid
import io.circe.Json
import sangria.ast.Document
import sangria.marshalling.InputUnmarshaller

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

  /**
    * Inlined from sangria-circe 1.2.1, as it is not yet available for circe 0.11.x and it is unclear when it would be.
    *
    * We only need this part so no need to inline everything else from `sangria.marshalling.circe._`.
    */
  implicit object CirceInputUnmarshaller extends InputUnmarshaller[Json] {
    def getRootMapValue(node: Json, key: String) = node.asObject.get(key)

    def isMapNode(node: Json) = node.isObject
    def getMapValue(node: Json, key: String) = node.asObject.get(key)
    def getMapKeys(node: Json) = node.asObject.get.keys

    def isListNode(node: Json) = node.isArray
    def getListValue(node: Json) = node.asArray.get

    def isDefined(node: Json) = !node.isNull
    def getScalarValue(node: Json) = {
      def invalidScalar =
        throw new IllegalStateException(s"$node is not a scalar value")

      node.fold(
        jsonNull = invalidScalar,
        jsonBoolean = identity,
        jsonNumber = num => num.toBigInt orElse num.toBigDecimal getOrElse invalidScalar,
        jsonString = identity,
        jsonArray = _ => invalidScalar,
        jsonObject = _ => invalidScalar
      )
    }

    def getScalaScalarValue(node: Json) = getScalarValue(node)

    def isEnumNode(node: Json) = node.isString

    def isScalarNode(node: Json) =
      node.isBoolean || node.isNumber || node.isString

    def isVariableNode(node: Json) = false
    def getVariableName(node: Json) =
      throw new IllegalArgumentException("variables are not supported")

    def render(node: Json) = node.noSpaces
  }
}
