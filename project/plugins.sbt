// formatting
addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.12")

// releasing
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")
addSbtPlugin("com.dwijnand" % "sbt-travisci" % "1.1.1")

// plugin project
libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"

// testing
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
