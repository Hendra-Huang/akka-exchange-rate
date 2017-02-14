name := "akka-exchange-rate-sample"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  ws,
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)
