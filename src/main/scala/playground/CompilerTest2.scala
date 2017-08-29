package playground

import scala.tools.nsc.Settings
import scala.tools.nsc.Global
import scala.tools.nsc.reporters.ConsoleReporter
import java.io.File
import scala.io.Source
import scala.reflect.io.AbstractFile
import scala.reflect.internal.util.SourceFile
import java.nio.file.Files
import java.nio.file.Paths
import scala.reflect.io.VirtualDirectory
import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.internal.util.AbstractFileClassLoader


// https://groups.google.com/forum/#!topic/scala-user/lLRp6f-n3to  - This is the code here.
// https://stackoverflow.com/questions/28274030/parsing-scala-source-with-scala-tools-nsc-global  - This only shows parsing.


object CompilerTest2 extends App {
//  val sourceString = "package a.b.c\n class HelloWorld extends (() => Unit) {  def apply() : Unit = println(\"Hello World!\") }"
  
  val sourceString = """
package a.b.c
class HelloWorld extends (() => Unit) {
  def apply() : Unit = println("Hello World!") 
}"""
  
  val inputFileName = "HelloWorld.scala"
  val inputFile = new BatchSourceFile(inputFileName, sourceString)

  val settings = new Settings
  settings.usejavacp.value = true
  val outputDirName = "myOutputDir"
  val outputDir = new VirtualDirectory(outputDirName, None)
  settings.outputDirs.setSingleOutput(outputDir)
  val global = Global(settings, new ConsoleReporter(settings))

  val run = new global.Run
  val parser = new global.syntaxAnalyzer.SourceFileParser(inputFile)
  val tree = parser.parse
  println(tree)

  run.compileSources(List(inputFile))

  val outputFileName = "HelloWorld.class"
  val outputFile =
    outputDir.lookupName("a", true).lookupName("b", true).lookupName("c", true).lookupName(outputFileName, false)

  val classBytes = outputFile.toByteArray
  println("Number of bytes in output file: "+classBytes.length)
  
  // Load and execute
  val classLoader = new AbstractFileClassLoader(outputDir, this.getClass.getClassLoader)
  val cls = classLoader.loadClass("a.b.c.HelloWorld")
  val obj = cls.getConstructor().newInstance().asInstanceOf[() => Unit]
  println(obj)
  obj()
}