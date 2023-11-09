ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "cluster-sender",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster-typed" % "2.6.19",
      "ch.qos.logback" % "logback-classic" % "1.4.3",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.19",
      "net.sf.json-lib" % "json-lib" % "2.4" classifier "jdk15",
      "com.typesafe.play" %% "play-json" % "2.9.4"
    )
  )
