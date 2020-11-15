package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.renderer.Renderer.FontData

case class PlotLabel(  labels: PlotStringSeries
                     , xPos: PlotDoubleSeries
                     , yPos: PlotDoubleSeries
                     , colors: PlotIntSeries = BlackARGB
                     , fonts: PlotFontSeries = FontData("Ariel", Renderer.FontStyle.Plain)
                     , widths: PlotDoubleSeries = 30
                     , heights: PlotDoubleSeries = 10
                     , xNudges: PlotDoubleSeries = 0
                     , yNudges: PlotDoubleSeries = -20
                     , boxed: Boolean = false) {
   def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
  yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): Unit = {

     val xNAxis = xAxis.asInstanceOf[NumericAxis]
     val yNAxis = yAxis.asInstanceOf[NumericAxis]
     val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
       xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
     val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
       yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
     val (start, end) = calcStartEnd
     for(i <- start until end) {
       val txt = labels(i)
       val (x, y) = (xPos(i), yPos(i))
       val col = colors(i)
       val font = fonts(i)
       val (width, height) = (widths(i), heights(i))
       val fs = r.maxFontSize(Seq(txt), width, height, font)
       val drawX = xConv(x) + xNudges(i)
       val drawY = yConv(y) + yNudges(i)
       if(boxed) {
         r.setColor(WhiteARGB)
         r.fillRectangle(drawX - width * 0.5, drawY - height * 0.5, width, height)
         r.setColor(BlackARGB)
         r.drawRectangle(drawX - width * 0.5, drawY - height * 0.5, width, height)
       }
       r.setFont(font, fs)
       r.setColor(col)
       r.drawText(txt, drawX, drawY, Renderer.HorizontalAlign.Center, 0.0)
     }
   }

  def calcStartEnd: (Int, Int) = (Array(labels, xPos, yPos, colors, fonts).map(_.minIndex).max, Array(labels, xPos, yPos, colors, fonts).map(_.maxIndex).min)
}
