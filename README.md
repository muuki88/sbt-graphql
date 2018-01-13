# sbt-graphql [![Build Status](https://travis-ci.org/muuki88/sbt-graphql.svg?branch=master)](https://travis-ci.org/muuki88/sbt-graphql) [ ![Download](https://api.bintray.com/packages/sbt/sbt-plugin-releases/sbt-graphql/images/download.svg) ](https://bintray.com/sbt/sbt-plugin-releases/sbt-graphql/_latestVersion) 

> This plugin is an experiment at this moment.
> SBT 1.x only

SBT plugin to generate and validate graphql schemas written with Sangria.

See also: https://github.com/mediative/sangria-codegen

# Goals

This plugin is intended for testing pipelines that ensure that your graphql
schema and queries are intact and match. You should also be able to compare
it with another schema, e.g. the production schema, to avoid breaking changes.

# Features

All features are based on the excellent [Sangria GraphQL library](http://sangria-graphql.org)

* Schema generation - inspired by [mediative/sangria-codegen](https://github.com/mediative/sangria-codegen)
* Schema validation - [sangria schema validation](http://sangria-graphql.org/learn/#schema-validation)
* Schema validation against another schema - [sangria schema comparison](http://sangria-graphql.org/learn/#schema-comparison)
* Schema release note generation
* Query validation against your locally generated schema - [sangria query validation](http://sangria-graphql.org/learn/#query-validation)

# Usage

Add this to your `plugins.sbt` and replace the `<version>` placeholder with the latest release.

```scala
addSbtPlugin("rocks.muki" % "sbt-graphql" % "<version>")
```

In your `build.sbt` enable the plugins and add sangria. I'm using circe as a parser for my json response.

```scala
enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.1.0"
)
``` 

## Schema generation

The schema is generated by accessing the application code via a generated main class that renders
your schema. The main class accesses your code via a small code snippet defined in `graphqlSchemaSnippet`.

Example:
My schema is defined in an object called `ProductSchema` in a field named `schema`.
In your `build.sbt` add

```scala
graphqlSchemaSnippet := "example.ProductSchema.schema"
``` 

Now you can generate a schema with

```bash
$ sbt graphqlSchemaGen
```

You can configure the output directory in your `build.sbt` with

```scala
target in graphqlSchemaGen := target.value / "graphql-build-schema"
```

## Schema definitions

Your build can contain multiple schemas. They are stored in the `graphqlSchemas` setting.
This allows to compare arbitrary schemas, write schema.json files for each of them and validate
your queries against them.
 
There is already one schemas predefined. The `build` schema is defined by the `graphqlSchemaGen` task.
You can configure the `graphqlSchemas` label with

```sbt
name in graphqlSchemaGen := "local-build"
```

### Add a schema

Schemas are defined via a `GraphQLSchema` case class. You need to define

* a `label`. It should be unique and human readable. `prod` and `build` already exist
* a `description`. Explain where this schema comes from and what it represents
* a `schemaTask`. A sbt task that generates the schema

You can also define a schema from a `SchemaLoader`. This requires defining an anonymous sbt task.

```scala
graphqlSchemas += GraphQLSchema(
  "sangria-example",
  "staging schema at http://try.sangria-graphql.org/graphql",
  Def.task(
    GraphQLSchemaLoader
      .fromIntrospection("http://try.sangria-graphql.org/graphql", streams.value.log)
      .loadSchema()
  ).taskValue
)
```

`sbt-graphql` provides a helper object `GraphQLSchemaLoader` to load schemas from different
places.

```scala
// from a file
graphqlProductionSchema := GraphQLSchemaLoader
  .fromFile((resourceManaged in Compile).value / "prod.graphql")
  .loadSchema()

// from a graphql endpoint via introspection
graphqlProductionSchema := GraphQLSchemaLoader
  .fromIntrospection("http://prod.your-graphql.net/graphql", streams.value.log)
  .withHeaders("X-Api-Version" -> "1", "X-Api-Key" -> "4198ab84-e992-42b0-8742-225ed15a781e")
  .loadSchema()
```

The introspection query doesn't support headers at this moment, but will be added
soon.


## Schema comparison

Sangria provides an API for comparing two Schemas. A change can be breaking or not.
The `graphqlValidateSchema` tasks compares two given schemas defined in the `graphqlSchemas` setting.

```bash
graphqlValidateSchema <old schema> <new schema>
```

### Example

You can compare the `build` and `prod` schema with

```bash
$ sbt
> graphqlValidateSchema build prod
```

## Schema rendering

You can render every schema with the `graphqlRenderSchema` task. In your sbt shell

```sbt
> graphqlRenderSchema build
```

This will render the `build` schema.

You can configure the target directory with

```scala
target in graphqlRenderSchema := target.value / "graphql-schema"
```

## Schema release notes

`sbt-graphql` creates release notes from changes between two schemas. The format is currently markdown.

```bash
$ sbt 
> graphqlReleaseNotes <old schema> <new schema>
```

### Example

You can create release notes for the `build` and `prod` schema with

```bash
$ sbt
> graphqlReleaseNotes build prod
```

## Query validation

The query validation uses the schema generated with `graphqlSchemaGen` to validate against all
graphql queries defined under `src/main/graphql`. Using separated `graphql` files for queries
is inspired by [apollo codegen](https://github.com/apollographql/apollo-codegen) which generates
typings for various languages.

To validate your graphql files run

```bash
sbt graphqlValidateQueries
```

You can change the source directory for your graphql queries with this line in
your `build.sbt`

```scala
sourceDirectory in (Compile, graphqlValidateQueries) := file("path/to/graphql")
```

# Developing

## Test project

You can try out your changes immediately with the `test-project`:

```bash
$ cd test-project
sbt
```

If you change code in the plugin you need to `reload` the test-project.

## Releasing

Push a tag `vX.Y.Z` a travis will automatically release it.
If you push to the `snapshot` branch a snapshot version (using the git sha)
will be published.

The `git.baseVersion := "x.y.z"` setting configures the base version for
snapshot releases.