import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

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
    buildInfoPackage := "pt.porchgeese.containers4s"
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(core)
