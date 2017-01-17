organization in ThisBuild := "com.chriswk"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "3.3"

lazy val gameranker = (project in file("."))
  .aggregate(gamerankerPlayerApi, gamerankerPlayerImpl)
  .settings(name := "gameranker")

lazy val gamerankerUtils = (project in file ("gameranker-utils"))
    .settings(
      version := "1.0-SNAPSHOT",
      libraryDependencies ++= Seq(
        lagomScaladslApi,
        lagomScaladslServer % Optional,
        playJsonDerivedCodecs,
        scalaTest
      )

    )

lazy val gamerankerSecurity = (project in file ("gameranker-security"))
    .settings(
      version := "1.0-SNAPSHOT",
      libraryDependencies ++= Seq(
        lagomScaladslApi,
        lagomScaladslServer % Optional,
        playJsonDerivedCodecs,
        scalaTest
      )
    )

lazy val gamerankerPlayerApi = (project in file("gameranker-player-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  ).dependsOn(gamerankerUtils)

lazy val gamerankerPlayerImpl = (project in file("gameranker-player-impl"))
  .enablePlugins(LagomScala)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      macwire,
      scalaTest,
      lagomScaladslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(gamerankerPlayerApi)