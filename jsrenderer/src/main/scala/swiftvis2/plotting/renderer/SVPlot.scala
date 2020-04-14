package swiftvis2.plotting.renderer

import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import swiftvis2.plotting._
import scala.collection.mutable
import scalajs.js

object SVPlot {
  // @JSExportTopLevel("simple")
  // def simple = Plot.simple _

  @JSExportTopLevel("scatterPlot")
  def scatterPlot(x: js.Array[Double], y: js.Array[Double]) = Plot.scatterPlot(x.toArray.toSeq, y.toArray.toSeq)

  @JSExportTopLevel("render")
  def render(plot: Plot, canvas: html.Canvas): Unit = {
    val r = new JSRenderer(canvas)
    plot.render(r, Bounds(0, 0, canvas.width, canvas.height))
  }
}