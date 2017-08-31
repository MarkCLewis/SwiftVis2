fork := true

lazy val root = (project in file("."))
  .settings(
    name         := "SwiftVis2",
    organization := "edu.trinity",
    scalaVersion := "2.12.3",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    version      := "0.1.0-SNAPSHOT",
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
		libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11"
  )
