package rocks.muki.graphql.codegen

import sbt._
import java.io.File

import scala.meta._

/**
  * == Source Code Writer ==
  *
  * Writes scalameta ASTs to scala source files.
  *
  */
object SourceCodeWriter {

  /**
    * Writes the `sourceCode` to a new scala source file.
    * The source file is produced by
    *
    *
    * @param context code generation context
    * @param graphqlFile the input graphql file
    * @param sourceCode the generated source code
    * @return the generated scala source file
    */
  def write(context: CodeGenContext, graphqlFile: File, sourceCode: Pkg): File = {
    val fileName = graphqlFile.getName.replaceAll("\\.graphql$|\\.gql$", ".scala").capitalize
    val outputFile = context.targetDirectory / fileName
    IO.write(outputFile, sourceCode.show[Syntax])
    outputFile
  }


  /**
    * Writes the `sourceCode` to the given `file`.
    * @param file the destination file
    * @param sourceCode the source code
    * @return the target file
    */
  def write(file: File, sourceCode: Pkg): File = {
    IO.write(file, sourceCode.show[Syntax])
    file
  }

}
