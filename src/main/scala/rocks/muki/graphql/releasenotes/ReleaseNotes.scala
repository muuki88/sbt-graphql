package rocks.muki.graphql.releasenotes

import sangria.schema.SchemaChange

trait ReleaseNotes {

  /**
    * Build the release changes
    *
    * @param changes the schema changes
    * @return human readable changelist. The format depends on the concrete implementation
    */
  def generateReleaseNotes(changes: Vector[SchemaChange]): String

}
