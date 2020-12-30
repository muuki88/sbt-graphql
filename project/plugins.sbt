// formatting
<<<<<<< HEAD
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
=======
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.4")
>>>>>>> master

// releasing
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")
addSbtPlugin("com.dwijnand" % "sbt-travisci" % "1.1.1")

// testing
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
