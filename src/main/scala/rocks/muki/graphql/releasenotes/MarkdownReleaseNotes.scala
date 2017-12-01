package rocks.muki.graphql.releasenotes
import sangria.schema.SchemaChange

class MarkdownReleaseNotes extends ReleaseNotes {

  /**
    * Build the release changes
    *
    * @param changes the schema changes
    * @return human readable changelist as markdown
    */
  override def generateReleaseNotes(changes: Vector[SchemaChange]): String = {
    val (breaking, nonBreaking) = changes.partition(_.breakingChange)

    val output = new StringBuffer()

    if (breaking.nonEmpty) {
      output.append("## Breaking schema changes\n")
      output.append(renderChanges(breaking))
      output.append("\n")
    }

    if(nonBreaking.nonEmpty) {
      output.append("## Schema changes\n")
      output.append(renderChanges(nonBreaking))
      output.append("\n")

    }

    output.toString
  }

  private def renderChanges(changes: Vector[SchemaChange]): String = {
    changes.map(change => s"* ${change.description}").mkString("\n")
  }
}
