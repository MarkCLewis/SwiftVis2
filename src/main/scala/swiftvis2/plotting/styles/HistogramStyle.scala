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

// TODO : Add the ability to swap axes. This will allow a histogram that is comparable to a box plot or a violin plot

/**
 * This style will draw out a histogram.
 */
final case class HistogramStyle(
    binValues: PlotDoubleSeries,
    valSourceColor: Seq[HistogramStyle.DataAndColor], 
    centerOnBins: Boolean = false,
    binsOnX: Boolean = true) extends NumberNumberPlotStyle {

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
      yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()
    
    require(end - start > 1, s"Empty data sent to histogram. start = $start, end = $end")

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
      
    val (binsConv, valConv) = if(binsOnX) (xConv, yConv) else (yConv, xConv)
    for (i <- start until end) {
      val values = valSourceColor.map(vs => vs.data(i))
      val (sbin, ebin) = if(centerOnBins) { 
        if (i == start) {
          (binsConv(binValues(i) - (binValues(i + 1) - binValues(i)) / 2), binsConv((binValues(i) + binValues(i + 1)) / 2))
        } else if (i == end - 1) {
          (binsConv((binValues(i) + binValues(i - 1)) / 2), binsConv(binValues(i) + (binValues(i) - binValues(i - 1)) / 2))
        } else {
          (binsConv((binValues(i) + binValues(i - 1)) / 2), binsConv((binValues(i) + binValues(i + 1)) / 2))
        }
      } else {
        (binsConv(binValues(i)), binsConv(binValues(i+1)))
      }
      var lastValue = 0.0
      for (j <- values.indices) {
        r.setColor(valSourceColor(j).color)
        val value = values(j) + lastValue
        val clasty = valConv(lastValue)
        val cy = valConv(value)
        if(binsOnX) {
          r.fillRectangle(sbin min ebin, clasty min cy, (ebin - sbin).abs, (clasty - cy).abs)
        } else {
          r.fillRectangle(clasty min cy, sbin min ebin, (clasty - cy).abs, (sbin - ebin).abs)
        }
        lastValue = value
      }
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }

  def calcStartEnd(): (Int, Int) = {
    ((valSourceColor.map(_.data) :+ binValues).map(_.minIndex).max,
     (valSourceColor.map(_.data) :+ binValues).map(_.maxIndex).min)
  }

  def xDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMin(start, end))
  }
  def xdMin(start: Int, end: Int): Double = if(binsOnX) binMin(start, end) else valueMin(start, end)
  
  def xDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMax(start, end))
  }
  def xdMax(start: Int, end: Int): Double = if(binsOnX) binMax(start, end) else valueMax(start, end)
    
  def yDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMin(start, end))
  }
  def ydMin(start: Int, end: Int): Double = if(binsOnX) valueMin(start, end) else binMin(start, end)
  
  def yDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMax(start, end))
  }
  def ydMax(start: Int, end: Int): Double = if(binsOnX) valueMax(start, end) else binMax(start, end)

  def binMin(start: Int, end: Int): Double = if(centerOnBins) binValues(start) - (binValues(start + 1) - binValues(start)) / 2 else binValues(start)
  def binMax(start: Int, end: Int): Double = if(centerOnBins) binValues(end - 1) + (binValues(end - 1) - binValues(end - 2)) / 2 else binValues(end)
  def valueMin(start: Int, end: Int): Double = (start until end).foldLeft(Double.MaxValue)((d, a) => d min valSourceColor.map(_.data(a)).sum)
  def valueMax(start: Int, end: Int): Double = (start until end).foldLeft(Double.MinValue)((d, a) => d max valSourceColor.map(_.data(a)).sum)
}

object HistogramStyle {
  case class DataAndColor(data: PlotDoubleSeries, color: Int)
}