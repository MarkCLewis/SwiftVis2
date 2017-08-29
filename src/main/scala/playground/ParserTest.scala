package playground

import scala.tools.reflect.ToolBox
import scala.reflect.runtime.universe
import universe._
import scala.tools.reflect.ReflectGlobal
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.reporters.ConsoleReporter


// https://scalerablog.wordpress.com/2016/06/20/scala-code-interpretation-at-runtime/
// https://stackoverflow.com/questions/12122939/generating-a-class-from-string-and-instantiating-it-in-scala-2-10/12123609#12123609

object ParserTest extends App {
  val tb = universe.runtimeMirror(this.getClass.getClassLoader()).mkToolBox()
  println(universe.showRaw(tb.parse("""
val in = new DataInput
import in._
x(3,0)
""")))
  speedTest(1)

  def speedTest(reps: Int): Unit = {
    val settings = new Settings
    val refGlobal = new ReflectGlobal(new Settings, new ConsoleReporter(settings), this.getClass.getClassLoader)
    import refGlobal._
    
//    refGlobal.newFreeTermSymbol("x", 6, 0, "") // Compiler says that this is deprecated
    val tree = tb.parse("val d=new swiftvis2.DataInput; 3+5*d.x(3,0)-4")
    println(tree)
    println(tb.eval(tree))
  }
}