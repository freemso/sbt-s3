import xerial.sbt.Sonatype._

ThisBuild / organization := "io.iftech"
ThisBuild / version := "1.0.0"
ThisBuild / description := "S3 Plugin for sbt."

isSnapshot := true

organization := "io.iftech"

organizationName := "IFTech"

startYear := Some(2019)

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    name := "sbt-s3",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.507",
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
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    sonatypeProjectHosting := Some(GitHubHosting("iftechio", "sbt-s3", "ouchengzu@iftech.io")),
    sonatypeProfileName := "io.iftech",
    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    isSnapshot := true,
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )
