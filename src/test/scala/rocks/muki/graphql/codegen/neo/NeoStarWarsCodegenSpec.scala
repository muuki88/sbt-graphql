package rocks.muki.graphql.codegen.neo

import rocks.muki.graphql.codegen.NeoScalametaGenerator


class NeoStarWarsCodegenSpec extends NeoCodegenBaseSpec("neo/starwars", (fileName: String) => NeoScalametaGenerator(fileName, Nil))