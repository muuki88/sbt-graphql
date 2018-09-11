package rocks.muki.graphql.codegen

import sbt._

import cats.syntax.either._

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
      // poor-mans traverse
      processedContent <- preProcessors.foldLeft(Right(input): Result[String]) {
        case (Right(processedContent), processor) =>
          processor(processedContent)
        case (error, _) => error
      }
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
    graphqlFiles
      .map(file => apply(file, targetDir, preProcessors))
      .foldLeft[Result[List[File]]](Right(List.empty)) {
        case (Left(failure), Left(nextFailure)) =>
          Left(Failure(failure.message + "\n" + nextFailure.message))
        case (Left(failure), _) => Left(failure)
        case (_, Left(failure)) => Left(failure)
        case (Right(files), Right(file)) => Right(files :+ file)
      }
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

    val processed = graphQLFile.split(IO.Newline).map {
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

    // poor mans cats.Validated
    val errors = processed.collect {
      case Left(error) => error
    }

    if (errors.isEmpty) {
      Right(
        processed
          .collect {
            case Right(line) => line
          }
          .mkString(IO.Newline))
    } else {
      Left(errors.reduce[Failure] {
        case (reduced, nextFailure) =>
          Failure(reduced.message + IO.Newline + reduced.message)
      })
    }
  }

}
