fork := true

lazy val commonSettings = Seq(
  organization := "edu.trinity",
  version := "0.1.0-SNAPSHOT",
  scalacOptions := Seq("-unchecked", "-deprecation"),
	homepage := Some(url("https://github.com/MarkCLewis/SwiftVis2")),
	scmInfo := Some(ScmInfo(url("https://github.com/MarkCLewis/SwiftVis2"),
                            "git@github.com:MarkCLewis/SwiftVis2.git")),
	developers := List(Developer("MarkCLewis",
                             "Mark Lewis",
                             "mlewis@trinity.edu",
                             url("https://github.com/MarkCLewis"))),
	licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
	publishMavenStyle := true,

// Add sonatype repository settings
	publishTo := Some(
	  if (isSnapshot.value)
	    Opts.resolver.sonatypeSnapshots
	  else
	    Opts.resolver.sonatypeStaging
	)
)

lazy val root = (project in file("."))
  .settings(
		commonSettings,
    name         := "SwiftVis2",
    crossScalaVersions := Seq("2.11.12", "2.12.6"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12",
//    libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )

lazy val spark = (project in file("spark"))
  .settings(
		commonSettings,
    name         := "SwiftVis2Spark",
    scalaVersion := "2.11.12",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.3.1",
    libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.3.1"
  ).dependsOn(root)
  
