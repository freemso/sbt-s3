ThisBuild / organization := "io.iftech"
ThisBuild / version := "0.0.3"
ThisBuild / description := "S3 Plugin for sbt."

ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    name := "sbt-s3",
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := Some(organization.value),
    libraryDependencies ++= Seq("com.amazonaws" % "aws-java-sdk-s3" % "1.11.507",
      "commons-lang" % "commons-lang" % "2.6"),
    publishMavenStyle := false,
    isSnapshot := true
  )
