import sbt._

object Dependencies {
  val LogbackVersion    = "1.0.1"
  val Log4CatsVersion   = "1.1.1"
  val Log4sVersion      = "1.8.2"
  val CatsVersion = "2.0.0"
  val CatsEffectVersion = "2.2.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0"
  lazy val dockerTest = List(
    "com.github.docker-java" % "docker-java" % "3.2.5",
    "com.github.docker-java" % "docker-java-transport-httpclient5" % "3.2.5"
  )

  val cats = List(
    "org.typelevel" %% "cats-core" % CatsVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion,
    "org.typelevel" %% "cats-free" % CatsVersion
  )

  val logbackAndLog4s = List(
    "org.log4s"         %% "log4s"           % Log4sVersion,
    "ch.qos.logback"     % "logback-classic" % LogbackVersion,
    "io.chrisdavenport" %% "log4cats-core"   % Log4CatsVersion
  )
}
