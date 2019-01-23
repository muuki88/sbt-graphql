name := "sbt-graphql"
organization := "rocks.muki"
sbtPlugin := true
enablePlugins(SbtPlugin)

val circeVersion = "0.9.3"
val catsVersion = "1.4.0"
libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.4.2",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-testkit" % catsVersion % Test,
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.scalameta" %% "scalameta" % "4.0.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

// Sbt 1.2.8 uses version 0.10.4 of jawn-parser; however, circe brings in version 0.11.1.
// To avoid this conflict, we're shading all libraries leading to jawn-parser.
//
//[info] rocks.muki:sbt-graphql:0.12.0-SNAPSHOT
//[info]   +-io.circe:circe-core_2.12:0.9.3 [S]
//[info]   | ...
//[info]   |     
//[info]   +-io.circe:circe-parser_2.12:0.9.3 [S]
//[info]   | +-io.circe:circe-core_2.12:0.9.3 [S]
//[info]   | | ...
//[info]   | |     
//[info]   | +-io.circe:circe-jawn_2.12:0.9.3 [S]
//[info]   |   +-io.circe:circe-core_2.12:0.9.3 [S]
//[info]   |   | ...
//[info]   |   |     
//[info]   |   +-org.spire-math:jawn-parser_2.12:0.11.1 [S]

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("io.circe.**" -> "rocks.muki.shaded.io.@1").inAll,
  ShadeRule.rename("jawn.**" -> "rocks.muki.shaded.jawn.@1").inAll
)
assemblyJarName in assembly := s"${name.value}-shaded-${version.value}.jar"

addArtifact(
  Artifact(
    name="sbt-graphql-shaded",
    `type`="jar",
    extension="jar",
    classifier=None,
    configurations=Vector(Compile.toConfigRef),
    url=None),
  sbtassembly.AssemblyKeys.assembly)

publishArtifact in (Compile, packageBin) := false

// scripted test settings
scriptedLaunchOpts += "-Dproject.version=" + version.value

scalacOptions += "-Ypartial-unification"

// project meta data
licenses := Seq(
  "Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0"))
homepage := Some(url("https://github.com/muuki88/sbt-graphql"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/muuki88/sbt-graphql"),
    "scm:git@github.com:muuki88/sbt-graphql.git"
  ))

developers := List(
  Developer(
    id = "muuki88",
    name = "Nepomuk Seiler",
    email = "nepomuk.seiler@gmail.com",
    url = url("https://www.muki.rocks")
  ))

// bintray config
bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"

// git versioning
enablePlugins(GitVersioning)

// The BaseVersion setting represents the in-development (upcoming) version, as an alternative to SNAPSHOTS.
git.baseVersion := "0.7.0"

// Create a release for every release tag
val ReleaseTag = """^v([\d\.]+)$""".r
git.gitTagToVersionNumber := {
  case ReleaseTag(v) => Some(v)
  case _ => None
}

git.formattedShaVersion := {
  val suffix =
    git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value,
                                       git.uncommittedSignifier.value)

  git.gitHeadCommit.value map { _.substring(0, 7) } map { sha =>
    git.baseVersion.value + "-" + sha + suffix
  }
}

// says that we do not want to use the GPG tools installed on your computer, but rather the implementation that sbt-pgp
// ships with; in my experience this is a must, otherwise depending on the GPG tools you have, you won't be able to make
// it use a different pgp ring
useGpg := false
// forces a certain key to be used for signing by specifying its key
usePgpKeyHex("0341175F3C364000")
pgpPublicRing := baseDirectory.value / "project" / ".gnupg" / "pub.asc"
pgpSecretRing := baseDirectory.value / "project" / ".gnupg" / "sec.asc"
// Travis has the ability to set such env variables to be available in your build
pgpPassphrase := sys.env.get("PGP_PASS").map(_.toArray)

// ci commands
addCommandAlias("validateFormatting",
                "; scalafmt::test ; test:scalafmt::test ; sbt:scalafmt::test")
addCommandAlias("validate",
                "; clean ; update ; validateFormatting ; test ; scripted")
