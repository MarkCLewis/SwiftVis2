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

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, yAxis: Axis, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val start = Array(xSource, ySource, symbolWidth, symbolHeight, colorFunction, connectWithLines.map(_._1).getOrElse(UnboundDoubleSeries)).map(_.minIndex).max
    val end = Array(xSource, ySource, symbolWidth, symbolHeight, colorFunction, connectWithLines.map(_._1).getOrElse(UnboundDoubleSeries)).map(_.maxIndex).min
    
    val connectMap = connectWithLines.map(_ => collection.mutable.Map[Double, (Double, Double)]())

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x+bounds.width, 
        (start until end).foldLeft(Double.MaxValue)((d, a) => d min xSource(a)), (start until end).foldLeft(Double.MinValue)((d, a) => d max xSource(a)),
        Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y+bounds.height, bounds.y,
        (start until end).foldLeft(Double.MaxValue)((d, a) => d min ySource(a)), (start until end).foldLeft(Double.MinValue)((d, a) => d max ySource(a)),
        Axis.RenderOrientation.YAxis, r, axisBounds)
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
}