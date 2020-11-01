import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / organization := "pt.porchgeese"
ThisBuild / organizationName := "porchgeese"

lazy val core = (project in file("modules/core"))
  .settings(
    libraryDependencies ++= dockerTest ++ cats ++ logbackAndLog4s,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    buildInfoPackage := "pt.porchgeese.containers4s"
  )
  .enablePlugins(BuildInfoPlugin)

lazy val examples = (project in file("modules/examples"))
  .settings(
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    buildInfoPackage := "pt.porchgeese.containers4s",
    libraryDependencies ++= doobie,
    publish / skip := true
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(core)
