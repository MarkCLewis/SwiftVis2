package swiftvis2.plotting.renderer

import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.CanvasRenderingContext2D
import swiftvis2.plotting.{BlackARGB, Bounds, Plot, RedARGB}

import scala.scalajs.js.annotation.JSExport

@JSExport
object Test {
  @JSExport
  def main(canvas: Canvas): Unit = {
    val rend = new JSRenderer(canvas)
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    val sp = Plot.scatterPlot(xPnt, yPnt)
    sp.render(rend, Bounds(0, 0, canvas.width, canvas.height))
//    val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//    ctx.fillRect(0, 0, 100, 100)
  }
}
