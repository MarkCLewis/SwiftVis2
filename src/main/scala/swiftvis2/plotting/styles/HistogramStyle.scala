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
    valSourceColor: Seq[(PlotDoubleSeries, Int)]) extends PlotStyle {

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, yAxis: Axis, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val start = (valSourceColor.map(_._1) :+ binValues).map(_.minIndex).max
    val end = (valSourceColor.map(_._1) :+ binValues).map(_.maxIndex).min
    
    require(end - start > 1)

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      binValues(start) - (binValues(start + 1) - binValues(start)) / 2,
      binValues(end - 1) + (binValues(end - 1) - binValues(end - 2)) / 2,
      Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      (start until end).foldLeft(Double.MaxValue)((d, a) => d min valSourceColor.map(_._1(a)).sum),
      (start until end).foldLeft(Double.MinValue)((d, a) => d max valSourceColor.map(_._1(a)).sum),
      Axis.RenderOrientation.YAxis, r, axisBounds)
    for (i <- start until end) {
      val ys = valSourceColor.map(vs => vs._1(i))
      val (sx, ex) = if (i == start) {
        (xConv(binValues(i) - (binValues(i + 1) - binValues(i)) / 2), xConv((binValues(i) + binValues(i + 1)) / 2))
      } else if (i == end - 1) {
        (xConv((binValues(i) + binValues(i - 1)) / 2), xConv(binValues(i) + (binValues(i) - binValues(i - 1)) / 2))
      } else {
        (xConv((binValues(i) + binValues(i - 1)) / 2), xConv((binValues(i) + binValues(i + 1)) / 2))
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
}