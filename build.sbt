name := "sbt-graphql"
organization := "rocks.muki"
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
    git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)

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
addCommandAlias("validateFormatting", "; scalafmtCheckAll ; scalafmtSbtCheck ")
addCommandAlias("validate", "; clean ; update ; validateFormatting ; test ; scripted")
