fork := true

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

scalaVersion := "2.13.7"

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
    scalaVersion := "2.13.7",
    javaOptions += "-Dio.netty.tryReflectionSetAccessible=true",
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val jvm = (project in file("jvm"))
  .settings(
    commonSettings,
    name         := "SwiftVis2JVM",
    scalaVersion := "2.13.7"
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
    scalaVersion := "2.13.7",
    libraryDependencies += "org.scalafx" %% "scalafx" % "17.0.1-R26",
    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % "17" classifier osName
    )
  ).dependsOn(jvm)

lazy val swingrenderer = (project in file("swingrenderer"))
  .settings(
    commonSettings,
    name         := "SwiftVis2Swing",
    scalaVersion := "2.13.7",
    libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.10",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"
  ).dependsOn(jvm)

// lazy val polynote = (project in file("polynoteintegration"))
//   .settings(
//     commonSettings,
//     name         := "SwiftVis2Polynote",
//     scalaVersion := "2.13.7",
//     libraryDependencies += "org.polynote" %% "polynote-runtime" % "0.4.4"
//   ).dependsOn(swingrenderer)

lazy val spark = (project in file("spark"))
  .settings(
		commonSettings,
    name         := "SwiftVis2Spark",
    scalaVersion := "2.13.7",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.0",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.0"
  ).dependsOn(coreJVM)

lazy val manTests = (project in file("manualtesting"))
  .settings(
    commonSettings,
    name         := "SwiftVis2ManualTests",
    scalaVersion := "2.13.7",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ).dependsOn(coreJVM, fxrenderer, swingrenderer, spark)

lazy val jsrenderer = (project in file("jsrenderer"))
  .settings(commonSettings,
    name         := "SwiftVis2JS",
    scalaVersion := "2.13.7",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0"
  ).dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)

lazy val reactrenderer = (project in file("reactrenderer"))
  .settings(commonSettings,
    name         := "SwiftVis2React",
    scalaVersion := "2.13.7",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    //libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    libraryDependencies += "me.shadaj" %%% "slinky-core" % "0.6.8",
    libraryDependencies += "me.shadaj" %%% "slinky-web" % "0.6.8",
    //scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    //addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    scalacOptions += "-Ymacro-annotations"
  ).dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)
