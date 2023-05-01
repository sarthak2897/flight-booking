import play.core.PlayVersion.akkaVersion
import sbt.Keys.libraryDependencies

name := """TicketBooking"""
organization := "com.sarthak"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

lazy val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)
//resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

lazy val mongoDependencies = Seq(
  // Enable reactive mongo for Play 2.8
  "org.reactivemongo" %% "play2-reactivemongo" % "1.0.10-play28",
 // "org.reactivemongo" %% "reactivemongo" % "1.1.0-RC9",
  // Provide JSON serialization for reactive mongo
  //"org.reactivemongo" %% "reactivemongo-play-json-compat" % "1.0.1-play28",
  // Provide BSON serialization for reactive mongo
  "org.reactivemongo" %% "reactivemongo-bson-compat" % "0.20.13",
  // Provide JSON serialization for Joda-Time
  "com.typesafe.play" %% "play-json-joda" % "2.7.4")
//  "org.reactivemongo" %% "reactivemongo-bson-macros" % "0.20.13")

libraryDependencies += guice
libraryDependencies ++= (akkaDependencies ++ mongoDependencies)
//libraryDependencies += guice



libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.sarthak.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.sarthak.binders._"
