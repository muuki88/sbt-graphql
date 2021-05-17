name := "sbt-graphql"
organization := "de.mukis"
sbtPlugin := true
enablePlugins(SbtPlugin)

val circeVersion = "0.13.0"
val catsVersion = "2.1.1"
libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "2.0.0-RC1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-jackson28" % circeVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-testkit" % catsVersion % Test,
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.scalameta" %% "scalameta" % "4.3.9",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "org.typelevel" %% "cats-testkit-scalatest" % "1.0.1" % Test
)

// scripted test settings
scriptedLaunchOpts += "-Dproject.version=" + version.value

scalacOptions += "-Ypartial-unification"

// project meta data
sonatypeProfileName := "nepomuk.seiler"
licenses := Seq("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0"))
homepage := Some(url("https://github.com/muuki88/sbt-graphql"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/muuki88/sbt-graphql"),
    "scm:git@github.com:muuki88/sbt-graphql.git"
  )
)

developers := List(
  Developer(
    id = "muuki88",
    name = "Nepomuk Seiler",
    email = "nepomuk.seiler@gmail.com",
    url = url("https://www.muki.rocks")
  )
)

// ci commands
addCommandAlias("validateFormatting", "; scalafmtCheckAll ; scalafmtSbtCheck ")
addCommandAlias("validate", "; clean ; update ; validateFormatting ; test ; scripted")
