ThisBuild / scalaVersion := "3.6.4"
ThisBuild / organization := "com.keystone"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val kms = ( project in file ("."))
    .settings(
        name := "kms",
        libraryDependencies ++= Seq(
            // Testing
            "org.scalatest" %% "scalatest" % "3.2.19" % Test,
        ),

        // Ensure tests directory is recognized
        Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
        Test / scalaSource := baseDirectory.value / "tests" / "src" / "test" / "scala"
    )