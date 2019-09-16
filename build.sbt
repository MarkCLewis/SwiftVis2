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

lazy val core = (project in file("core"))
  .settings(
		commonSettings,
    name         := "Core",
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12",
    libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )

lazy val fxrenderer = (project in file("fxrenderer"))
  .settings(
                commonSettings,
    name         := "ScalaFXRenderer",
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  ).dependsOn(core)

lazy val swingrenderer = (project in file("swingrenderer"))
  .settings(
                commonSettings,
    name         := "SwingRenderer",
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  ).dependsOn(core)

lazy val spark = (project in file("spark"))
  .settings(
		commonSettings,
    name         := "SwiftVis2Spark",
    scalaVersion := "2.12.8",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "2.4.0",
    libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "2.4.0"
  ).dependsOn(core)

lazy val manTests = (project in file("manualtesting"))
  .settings(
    commonSettings,
    name         := "SwiftVis2ManualTests",
    crossScalaVersions := Seq("2.11.12", "2.12.8"),
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ).dependsOn(core, fxrenderer, swingrenderer, spark)
