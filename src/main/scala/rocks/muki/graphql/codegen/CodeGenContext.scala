package rocks.muki.graphql.codegen

import java.io.File

import sangria.schema.Schema
import sbt.Logger

/**
  * == CodeGen Context ==
  *
  * Initial context to kickoff code generation.
  *
  * @param schema the graphql schema
  * @param targetDirectory the target directory where the source code will be placed
  * @param graphQLFiles input files that should be processed
  * @param packageName the scala package name
  * @param moduleName optional module name for single-file based generators
  * @param jsonCodeGen
  * @param imports list of imports to be added to the generated file
  * @param preProcessors pre processors that should be applied before the graphql file is parsed
  * @param log output log
  */
case class CodeGenContext(
    schema: Schema[_, _],
    targetDirectory: File,
    graphQLFiles: Seq[File],
    packageName: String,
    moduleName: String,
    jsonCodeGen: JsonCodeGen,
    imports: Seq[String],
    preProcessors: Seq[PreProcessor],
    log: Logger
)
