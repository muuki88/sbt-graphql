package rocks.muki.graphql.schema

sealed trait SchemaFilterName {
  def name: String
}

/*
 * The set of filters available to be used in conjunction with `renderPretty` && `renderCompact`
 * These should follow the `SchemaFilter`(s) available in sangria, defined here:
 * https://github.com/sangria-graphql/sangria/blob/343d7a59eeb9392573751306f2b485bca2bee75f/src/main/scala/sangria/renderer/SchemaRenderer.scala#L298-L323
 */
object SchemaFilters {
  case object WithoutSangriaBuiltIn extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.withoutSangriaBuiltIn"
  }
  case object Default extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.default"
  }
  case object WithoutGraphQLBuiltIn extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.withoutGraphQLBuiltIn"
  }
  case object WithoutIntrospection extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.withoutIntrospection"
  }
  case object BuiltIn extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.builtIn"
  }
  case object Introspection extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.introspection"
  }
  case object All extends SchemaFilterName {
    val name = "sangria.renderer.SchemaFilter.all"
  }
}
