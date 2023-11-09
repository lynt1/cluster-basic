ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "cluster-basic",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster-typed" % "2.6.19",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19",
      "ch.qos.logback" % "logback-classic" % "1.4.3",
    ),
  )
