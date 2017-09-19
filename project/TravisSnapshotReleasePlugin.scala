import com.typesafe.sbt.{GitPlugin, GitVersioning, SbtPgp}
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.pgp.PgpKeys.publishSigned
import sbt._
import sbt.Keys._
import sbttravisci.TravisCiPlugin
import sbttravisci.TravisCiPlugin.autoImport._

object TravisSnapshotReleasePlugin extends AutoPlugin {

  override def trigger = PluginTrigger.AllRequirements
  override def requires = GitVersioning && TravisCiPlugin && SbtPgp && GitPlugin

  object autoImport {
    val publishViaTravis: TaskKey[Unit] =
      taskKey[Unit]("publish via travis CI")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    publishViaTravis := Def
      .taskDyn[Unit] {
        val log = streams.value.log
        val branch = git.gitCurrentBranch.value
        val currentTags = git.gitCurrentTags.value
        val tagToVersionNumber = git.gitTagToVersionNumber.value
        val releaseTag = git.releaseVersion(currentTags, tagToVersionNumber, "")
        if (!isTravisBuild.value) {
          Def.task[Unit] {
            log.info("Not running on travis. Skip publish task")
          }
        } else if (travisPrNumber.value.isDefined) {
          Def.task[Unit] {
            log.info("Building a pull request. Skip publish task")
          }
        } else if (branch == "snapshot") {
          Def
            .task[Unit] {
              val snapshotVersion = version.value
              log.info(
                s"Pushed to snapshot branch. Publishing $snapshotVersion")
            }
            .dependsOn(publishSigned)
        } else if (branch == "master" && releaseTag.isDefined) {
          Def
            .task[Unit] {
              log.info(
                s"Pushed to master branch. Publishing a release with version ${version.value}")
            }
            .dependsOn(publishSigned)
        } else {
          Def.task[Unit] {
            log.info(
              "Skip publishing from travis. No release tag found or not on snapshot branch")
            log.info(
              s"Branch $branch | Head commit: ${git.gitHeadCommit.value}")
          }
        }

      }
      .value
  )

}
