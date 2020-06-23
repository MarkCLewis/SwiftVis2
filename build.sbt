fork := true

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


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

lazy val core = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("core"))
  .settings(
		commonSettings,
    name         := "SwiftVis2Core",
    crossScalaVersions := Seq("2.12.11"),
    javaOptions += "-Dio.netty.tryReflectionSetAccessible=true",
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val jvm = (project in file("jvm"))
  .settings(
    commonSettings,
    name         := "SwiftVis2JVM",
    crossScalaVersions := Seq("2.12.11")
  ).dependsOn(coreJVM)

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

lazy val fxrenderer = (project in file("fxrenderer"))
  .settings(
    commonSettings,
    name         := "SwiftVis2FX",
    crossScalaVersions := Seq("2.12.11"),
    libraryDependencies += "org.scalafx" %% "scalafx" % "11-R16",
    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % "11" classifier osName
    )
  ).dependsOn(jvm)

lazy val swingrenderer = (project in file("swingrenderer"))
  .settings(
    commonSettings,
    name         := "SwiftVis2Swing",
    crossScalaVersions := Seq("2.12.11"),
    libraryDependencies += "org.scala-lang.modules" % "scala-swing_2.12" % "2.0.3",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  ).dependsOn(jvm)

lazy val spark = (project in file("spark"))
  .settings(
		commonSettings,
    name         := "SwiftVis2Spark",
    scalaVersion := "2.12.11",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"
  ).dependsOn(coreJVM)

lazy val manTests = (project in file("manualtesting"))
  .settings(
    commonSettings,
    name         := "SwiftVis2ManualTests",
    crossScalaVersions := Seq("2.12.11"),
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ).dependsOn(coreJVM, fxrenderer, swingrenderer, spark)

lazy val jsrenderer = (project in file("jsrenderer"))
  .settings(commonSettings,
    name         := "SwiftVis2JS",
    crossScalaVersions := Seq("2.12.11"),
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.7"
  ).dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)

lazy val reactrenderer = (project in file("reactrenderer"))
  .settings(commonSettings,
    name         := "SwiftVis2React",
    crossScalaVersions := Seq("2.12.11"),
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.7",
    libraryDependencies += "me.shadaj" %%% "slinky-core" % "0.6.5",
    libraryDependencies += "me.shadaj" %%% "slinky-web" % "0.6.5",
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  ).dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)