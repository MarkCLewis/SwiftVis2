fork := true

lazy val root = (project in file("."))
  .settings(
    name         := "SwiftVis2",
    organization := "edu.trinity",
    crossScalaVersions := Seq("2.11.8", "2.12.4"),
    scalacOptions := Seq("-unchecked", "-deprecation"),
    version      := "0.1.0-SNAPSHOT",
    libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value, 
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.102-R11",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )

lazy val spark = (project in file("spark"))
  .settings(
    name         := "SwiftVis2Spark",
    organization := "edu.trinity",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    version      := "0.1.0-SNAPSHOT",
    libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.3.0",
    libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.3.0"
  ).dependsOn(root)
  
