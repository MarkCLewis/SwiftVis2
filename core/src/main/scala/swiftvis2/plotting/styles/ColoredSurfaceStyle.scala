package swiftvis2.plotting.styles

import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer
import scala.collection.immutable.ArraySeq

case class XYC(x: Double, y: Double, c: Int)

case class ColoredSurfaceStyle(
    xSource: PlotDoubleSeries,
    ySource: PlotDoubleSeries,
    group: PlotSeries,
    colors: PlotIntSeries,
    labels: Seq[PlotLabel] = Seq.empty
  ) extends NumberNumberPlotStyle {

  def render(
      r: Renderer,
      bounds: Bounds,
      xAxis: Axis,
      xminFunc: Axis => Double,
      xmaxFunc: Axis => Double,
      yAxis: Axis,
      yminFunc: Axis => Double,
      ymaxFunc: Axis => Double,
      axisBounds: Seq[Bounds]
  ): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    
    val pData = Array.fill(2)(collection.mutable.ArrayBuffer[XYC]())
    var lastGroupValue = group(start)

    for (i <- start until end) {
      val x = xConv(xSource(i))
      val y = yConv(ySource(i))
      val color = colors(i)
      val g = group(i)
      if (g != lastGroupValue) {
        drawGroup()
        lastGroupValue = g
      }
      pData(1) += XYC(x, y, color)
    }
    drawGroup()

    def drawGroup(): Unit = {
      for (i <- 0 until pData(0).length - 1; if i < pData(1).length - 1) {
        r.setColor(pData(0)(i).c)
        val xs = ArraySeq(pData(0)(i).x, pData(1)(i).x, pData(1)(i + 1).x, pData(0)(i + 1).x)
        val ys = ArraySeq(pData(0)(i).y, pData(1)(i).y, pData(1)(i + 1).y, pData(0)(i + 1).y)
        r.fillPolygon(xs, ys)
        r.drawPolygon(xs, ys)
      }
      
      // Swap data down.
      val tmp = pData(0)
      pData(0) = pData(1)
      pData(1) = tmp
      pData(1).clear()
    }

    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }

  def calcStartEnd(): (Int, Int) = {
    (Array(xSource, ySource, group, colors).map(_.minIndex).max,
      Array(xSource, ySource, group, colors).map(_.maxIndex).min)
  }

  def xDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMin(start, end))
  }
  def xdMin(start: Int, end: Int): Double = (start until end).foldLeft(Double.MaxValue)((d, a) => d min xSource(a))

  def xDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMax(start, end))
  }
  def xdMax(start: Int, end: Int): Double = (start until end).foldLeft(Double.MinValue)((d, a) => d max xSource(a))

  def yDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMin(start, end))
  }
  def ydMin(start: Int, end: Int): Double = (start until end).foldLeft(Double.MaxValue)((d, a) => d min ySource(a))

  def yDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMax(start, end))
  }
  def ydMax(start: Int, end: Int): Double = (start until end).foldLeft(Double.MinValue)((d, a) => d max ySource(a))

  def legendFields: Seq[LegendItem] = List.empty
}