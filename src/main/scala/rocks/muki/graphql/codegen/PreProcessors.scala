package rocks.muki.graphql.codegen

import cats.implicits._
import sbt._

object PreProcessors {

  /**
    * Applies all preprocessors and returns a new file with the processed content
    *
    * @param graphqlFile the input graphql file
    * @param targetDir target directory where the processed file should be written
    * @param preProcessors the list of preprocessors
    * @return a new file with the processed content
    */
  def apply(graphqlFile: File,
            targetDir: File,
            preProcessors: Seq[PreProcessor]): Result[File] = {
    val processedFile = targetDir / graphqlFile.getName

    for {
      input <- Either.catchNonFatal(IO.read(graphqlFile)).leftMap { error =>
        Failure(s"Failed to read $graphqlFile: ${error.getMessage}")
      }
      /*_*/ // Iteratively apply all preprocessors after one another. Fail-fast semantics through Either.
      processedContent <- preProcessors.toList.foldM(input)(
        (previousValue, processor) => processor(previousValue)
      )
      /*_*/
    } yield {
      IO.write(processedFile, processedContent)
      processedFile
    }
  }

  /**
    * Applies the preprocessors to all given files
    *
    * @param graphqlFiles graphql input files
    * @param targetDir target directory where the processed file should be written
    * @param preProcessors the list of preprocessors
    * @return a list of new files with the processed content
    */
  def apply(graphqlFiles: Seq[File],
            targetDir: File,
            preProcessors: Seq[PreProcessor]): Result[Seq[File]] = {

    /*_*/ // Apply preprocessor list to all files. Accumulated errors through ValidatedNel.
    graphqlFiles.toList
      .traverse(file => apply(file, targetDir, preProcessors).toValidatedNel)
      .toEither
      .leftMap(errorMessages => errorMessages.reduce) // reduce NonEmptyList[Failure] to one Failure
    /*_*/
  }

  /**
    * Allows to import other graphql files (e.g. fragments) into an graphql file.
    *
    * Allowed characters in the path are
    * - everything that matches `\w`, e.g. characters, decimals and _
    * - forward slashes `/`
    * - dots `.`
    *
    * @example {{{
    *    #import fragments/foo.graphql
    * }}}
    * @param rootDirectories a list of directories that should be used to resolve magic imports
    * @return
    */
  def magicImports(rootDirectories: Seq[File]): PreProcessor = graphQLFile => {
    // match the import file path
    val Import = "#import\\s*([\\w\\/\\.]*)".r

    val processedLines = graphQLFile.split(IO.Newline).map {
      case Import(filePath) =>
        rootDirectories.map(dir => dir / filePath).find(_.exists()) match {
          case Some(importedGraphQLFile) =>
            val importedGraphQLContent = IO.read(importedGraphQLFile)
            for {
              recursiveProcessed <- magicImports(rootDirectories)(
                importedGraphQLContent)
            } yield recursiveProcessed + IO.Newline
          case None =>
            Left(Failure(s"Could not resolve $filePath in $rootDirectories"))
        }
      case line => Right(line)
    }

    /*_*/
    processedLines.toList
      .traverse(_.toValidatedNel)
      .toEither
      .bimap(
        errorMessages => errorMessages.reduce,
        lines => lines.mkString(IO.Newline)
      )
    /*_*/
  }

}
