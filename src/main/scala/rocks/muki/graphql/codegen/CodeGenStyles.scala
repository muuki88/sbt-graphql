package rocks.muki.graphql.codegen

import java.io.File

import sbt._

import scala.meta._
import sangria.ast

/**
  * == CodeGen Styles ==
  *
  * Object that contains different code generation styles
  *
  */
object CodeGenStyles {

  type Style = CodeGenContext => Seq[File]

  /**
    * == Apollo CodeGen style ==
    *
    * Generates a source file per graphql input file.
    * Every query will extend the `GraphQLQuery` trait to allow a generic client implementation.
    *
    */
  val Apollo: Style = context => {
    val schema = context.schema
    val inputFiles = context.graphQLFiles
    val packageName = ScalametaUtils.termRefOf(context.packageName)

    // Generate the GraphQLQuery trait
    val graphQLQueryFile = context.targetDirectory / s"${GraphQLQueryGenerator.name}.scala"
    SourceCodeWriter.write(graphQLQueryFile, GraphQLQueryGenerator.sourceCode(context.packageName))

    val additionalImports = ScalametaUtils.imports(context.imports.toList)
    val additionalInits = GraphQLQueryGenerator.inits

    // Process all the graphql files
    val files = inputFiles.map { inputFile =>
      for {
        processedFile <- PreProcessors(inputFile, context.targetDirectory, context.preProcessors)
        queryDocument <- DocumentLoader.single(schema, processedFile)
        typedDocument <- TypedDocumentParser(schema, queryDocument)
          .parse()
        sourceCode <- ApolloSourceGenerator(inputFile.getName, additionalImports, additionalInits, context.jsonCodeGen)(
          typedDocument
        )
      } yield {
        val stats =
          q"""package $packageName {
               ..$sourceCode
             }"""

        val outputFile = SourceCodeWriter.write(context, inputFile, stats)
        context.log.debug(s"Generated source $outputFile from $inputFile ")
        outputFile
      }
    }

    val interfaceFile = for {
      // use all queries to determine the interfaces & types we need
      processedFiles <- PreProcessors(inputFiles, context.targetDirectory, context.preProcessors)
      allQueries <- DocumentLoader.merged(schema, processedFiles.toList)
      typedDocument <- TypedDocumentParser(schema, allQueries)
        .parse()
      codeGenerator = ApolloSourceGenerator("Interfaces.scala", additionalImports, additionalInits, context.jsonCodeGen)
      interfaces <- codeGenerator.generateInterfaces(typedDocument)
      types <- codeGenerator.generateTypes(typedDocument)
    } yield {
      val stats = q"""package $packageName {
             ..$interfaces
             ..$types
         }
         """
      val outputFile = context.targetDirectory / "Interfaces.scala"
      SourceCodeWriter.write(outputFile, stats)
      context.log.debug(s"Generated source $outputFile")
      outputFile
    }

    val allFiles = files :+ interfaceFile

    // split errors and success
    val success = allFiles.collect {
      case Right(file) => file
    }

    val errors = allFiles.collect {
      case Left(error) => error
    }

    if (errors.nonEmpty) {
      context.log.err(s"${errors.size} error(s) during code generation")
      errors.foreach(error => context.log.error(error.message))
      sys.error("Code generation failed")
    }

    // return all generated files
    success :+ graphQLQueryFile
  }

  val Sangria: Style = context => {
    val schema = context.schema
    val inputFiles = context.graphQLFiles
    val packageName = ScalametaUtils.termRefOf(context.packageName)

    val result = for {
      queryDocument <- DocumentLoader.merged(schema, inputFiles.toList)
      typedDocument <- TypedDocumentParser(schema, queryDocument)
        .parse()
      sourceCode <- ScalametaGenerator(context.moduleName)(typedDocument)
    } yield {
      val sourceCodeStats: List[Stat] = List(sourceCode)
      val pkg =
        q"""package $packageName {
               ..$sourceCodeStats
             }"""

      val outputFile = context.targetDirectory / s"${context.moduleName.capitalize}.scala"
      SourceCodeWriter.write(outputFile, pkg)
    }

    result match {
      case Right(file) =>
        List(file)
      case Left(error) =>
        context.log.err(s"Error during code generation $error")
        sys.error("Code generation failed")
    }

  }

}
