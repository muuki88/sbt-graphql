package rocks.muki.graphql.codegen

import sbt._
import java.io.File

import scala.meta._

object SourceCodeWriter {

  def write(context: CodeGenContext, graphqlFile: File, sourceCode: Pkg): File = {
    val fileName = graphqlFile.getName.replaceAll("\\.graphql$|\\.gql$", ".scala").capitalize
    val outputFile = context.targetDirectory / fileName
    IO.write(outputFile, sourceCode.show[Syntax])
    outputFile
  }


  def write(file: File, sourceCode: Pkg): File = {
    IO.write(file, sourceCode.show[Syntax])
    file
  }

}
