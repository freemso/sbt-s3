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
    scalacOptions in(Compile, doc) ++=
      Opts.doc.title(name.value + ": " + description.value) ++
        Opts.doc.version(version.value) ++
        Seq("-doc-root-content", (sourceDirectory.value / "main/rootdoc.txt").getAbsolutePath),
    publishTo := sonatypePublishTo.value,
    sonatypeProjectHosting := Some(GitHubHosting("emersonloureiro", "sbt-s3", "emerson.loureiro@gmail.com")),
    sonatypeProfileName := "cf.janga",
    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    isSnapshot := true,
    crossSbtVersions := Seq("0.13.18", "1.2.8"),
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
  )
