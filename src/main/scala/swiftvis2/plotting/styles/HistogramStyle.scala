package swiftvis2.plotting.styles

import swiftvis2.plotting.Axis
import swiftvis2.plotting.Bounds
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.UnboundDoubleSeries
import swiftvis2.plotting.NumericAxis
import swiftvis2.plotting.CategoryAxis
import swiftvis2.plotting.PlotStringSeries
import swiftvis2.plotting.PlotDoubleSeries
import swiftvis2.plotting.PlotIntSeries

case class HistogramStyle(
    binValues: PlotDoubleSeries,
    valSourceColor: Seq[(PlotDoubleSeries, Int)], centerOnBins: Boolean = false) extends PlotStyle {

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
      yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()
    
    require(end - start > 1)

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      xdMin(start, end), xdMax(start, end), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      ydMin(start, end), ydMax(start, end), Axis.RenderOrientation.YAxis, r, axisBounds)
    for (i <- start until end) {
      val ys = valSourceColor.map(vs => vs._1(i))
      val (sx, ex) = if(centerOnBins) { 
        if (i == start) {
          (xConv(binValues(i) - (binValues(i + 1) - binValues(i)) / 2), xConv((binValues(i) + binValues(i + 1)) / 2))
        } else if (i == end - 1) {
          (xConv((binValues(i) + binValues(i - 1)) / 2), xConv(binValues(i) + (binValues(i) - binValues(i - 1)) / 2))
        } else {
          (xConv((binValues(i) + binValues(i - 1)) / 2), xConv((binValues(i) + binValues(i + 1)) / 2))
        }
      } else {
        (xConv(binValues(i)), xConv(binValues(i+1)))
      }
      var lasty = 0.0
      for (j <- ys.indices) {
        r.setColor(valSourceColor(j)._2)
        val y = ys(j) + lasty
        val clasty = yConv(lasty)
        val cy = yConv(y)
        r.fillRectangle(sx, clasty min cy, ex - sx, (clasty - cy).abs)
        lasty = y
      }
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }

  def calcStartEnd(): (Int, Int) = {
    ((valSourceColor.map(_._1) :+ binValues).map(_.minIndex).max,
     (valSourceColor.map(_._1) :+ binValues).map(_.maxIndex).min)
  }

  def xDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMin(start, end))
  }
  def xdMin(start: Int, end: Int): Double = if(centerOnBins) binValues(start) - (binValues(start + 1) - binValues(start)) / 2 else binValues(start)
  
  def xDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMax(start, end))
  }
  def xdMax(start: Int, end: Int): Double = if(centerOnBins) binValues(end - 1) + (binValues(end - 1) - binValues(end - 2)) / 2 else binValues(end)
    
  def yDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMin(start, end))
  }
  def ydMin(start: Int, end: Int): Double = (start until end).foldLeft(Double.MaxValue)((d, a) => d min valSourceColor.map(_._1(a)).sum)
  
  def yDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMax(start, end))
  }
  def ydMax(start: Int, end: Int): Double = (start until end).foldLeft(Double.MinValue)((d, a) => d max valSourceColor.map(_._1(a)).sum)
}