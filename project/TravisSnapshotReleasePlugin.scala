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

    val travisBranch: SettingKey[Option[String]] =
      settingKey[Option[String]]("travis git branch")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    travisBranch := sys.env.get("TRAVIS_BRANCH"),
    publishViaTravis := Def
      .taskDyn[Unit] {
        val log = streams.value.log
        // Travis sets the branch via an environment variable. Also tags are checked out as branches
        val branch = travisBranch.value.getOrElse("<no travis branch>")
        val currentTags = List(branch)

        val tagToVersionNumber = git.gitTagToVersionNumber.value
        val releaseTag = git.releaseVersion(currentTags, tagToVersionNumber, "")
        log.info(s"Publish via travis on branch $branch")
        log.info(s"Using version ${version.value}")
        log.info(s"Release tag $releaseTag")

        if (!isTravisBuild.value) {
          Def.task[Unit] {
            log.warn("Not running on travis. Skip publish task")
          }
        } else if (travisPrNumber.value.isDefined) {
          Def.task[Unit] {
            log.warn("Building a pull request. Skip publish task")
          }
        } else if (branch == "snapshot") {
          Def
            .task[Unit] {
              val snapshotVersion = version.value
              log.success(
                s"Pushed to snapshot branch. Publishing $snapshotVersion")
            }
          //.dependsOn(publishSigned)
        } else if (releaseTag.isDefined) {
          Def
            .task[Unit] {
              log.success(
                s"Pushed to master branch. Publishing a release with version ${version.value}")
            }
          //.dependsOn(publishSigned)
        } else {
          Def.task[Unit] {
            log.warn(
              "Skip publishing from travis. No release tag found or not on snapshot branch")
            log.warn(
              s"Branch $branch | Head commit: ${git.gitHeadCommit.value}")
          }
        }

      }
      .value
  )

}
