package playground

import scala.reflect.runtime.universe
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.reflect.ReflectGlobal
import scala.tools.reflect.ToolBox

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

//    refGlobal.newFreeTermSymbol("x", 6, 0, "") // Compiler says that this is deprecated
    val tree = tb.parse("val d=new swiftvis2.DataInput; 3+5*d.x(3,0)-4")
    println(tree)
    println(tb.eval(tree))
  }
}
