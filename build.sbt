import xerial.sbt.Sonatype._

organization := "io.iftech"
organizationName := "IFTech"
version := "1.0.3.1-SNAPSHOT"
description := "S3 Plugin for sbt."
startYear := Some(2019)

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    name := "sbt-s3",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.892",
      "commons-lang" % "commons-lang" % "2.6",
      "javax.xml.bind" % "jaxb-api" % "2.2.11",
      "com.sun.xml.bind" % "jaxb-core" % "2.2.11",
      "com.sun.xml.bind" % "jaxb-impl" % "2.2.11",
      "javax.activation" % "activation" % "1.1.1"
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/iftechio/sbt-s3"),
        "scm:git@github.com:iftechio/sbt-s3.git"
      )
    ),
    publishTo := sonatypePublishToBundle.value,
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeProjectHosting := Some(GitHubHosting("iftechio", "sbt-s3", "ouchengzu@iftech.io")),
    sonatypeProfileName := "io.iftech",
    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )
