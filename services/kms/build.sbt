ThisBuild / scalaVersion := "3.6.4"
ThisBuild / organization := "com.keystone"
ThisBuild / version := "0.1.0-SNAPSHOT"


lazy val kms = ( project in file ("."))
    .settings(
        name := "kms",
        scalacOptions ++= Seq(
            "-Werror",
            "-unchecked",
            "-feature",
            "-deprecation"
        ),
        libraryDependencies ++= Seq(
            // Testing
            "org.scalatest" %% "scalatest" % "3.2.20" % Test,
        ),

        Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
    )