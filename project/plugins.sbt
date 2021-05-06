// formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// releasing
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")

// testing
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
