import sbt._

class GithubCodeFixer(info: ProjectInfo) extends DefaultProject(info) {
    val dispatch = "net.databinder" %% "dispatch-http" % "0.8.6"
    val liftJson = "net.liftweb" %% "lift-json" % "2.3"
}