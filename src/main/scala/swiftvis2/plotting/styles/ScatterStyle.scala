package swiftvis2.plotting.styles

import swiftvis2.plotting.Axis
import swiftvis2.plotting.Bounds
import swiftvis2.plotting.PlotDoubleSeries
import swiftvis2.plotting.PlotIntSeries
import swiftvis2.plotting.PlotSymbol
import swiftvis2.plotting.UnboundDoubleSeries
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.NumericAxis

case class ScatterStyle(
    xSource: PlotDoubleSeries,
    ySource: PlotDoubleSeries,
    symbol: PlotSymbol,
    symbolWidth: PlotDoubleSeries,
    symbolHeight: PlotDoubleSeries,
    xSizing: PlotSymbol.Sizing.Value,
    ySizing: PlotSymbol.Sizing.Value,
    colorFunction: PlotIntSeries,  // Index to ARGB
    connectWithLines: Option[(PlotDoubleSeries, Renderer.StrokeData)]
    ) extends PlotStyle {

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
      yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()
    
    val connectMap = connectWithLines.map(_ => collection.mutable.Map[Double, (Double, Double, Int)]())

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x+bounds.width, 
        xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y+bounds.height, bounds.y,
        yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    for(i <- start until end) {
      val x = xConv(xSource(i))
      val y = yConv(ySource(i))
      val width = symbolWidth(i)
      val height = symbolHeight(i)
      // TODO - include sizing code
      // TODO - add to connect map
      r.setColor(colorFunction(i))
      symbol.drawSymbol(x, y, width, height, r)
    }
    (connectWithLines, connectMap).zipped.foreach { case ((_, stroke), cm) =>
      // TODO - draw connections
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }
  
  def calcStartEnd(): (Int, Int) = {
    (Array(xSource, ySource, symbolWidth, symbolHeight, colorFunction, connectWithLines.map(_._1).getOrElse(UnboundDoubleSeries)).map(_.minIndex).max,
     Array(xSource, ySource, symbolWidth, symbolHeight, colorFunction, connectWithLines.map(_._1).getOrElse(UnboundDoubleSeries)).map(_.maxIndex).min)  
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
}