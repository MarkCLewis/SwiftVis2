package swiftvis2.plotting.styles

import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer

case class ScatterStyle(
    xSource: PlotDoubleSeries,
    ySource: PlotDoubleSeries,
    symbol: PlotSymbol,
    symbolWidth: PlotDoubleSeries,
    symbolHeight: PlotDoubleSeries,
    xSizing: PlotSymbol.Sizing.Value,
    ySizing: PlotSymbol.Sizing.Value,
    colorFunction: PlotIntSeries,
    connectWithLines: Option[(PlotDoubleSeries, Renderer.StrokeData)] = None,
    xErrorBars: Option[PlotDoubleSeries] = None,
    yErrorBars: Option[PlotDoubleSeries] = None) extends PlotStyle {

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
             yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()

    val connectMap = connectWithLines.map(_ => collection.mutable.Map[Double, List[(Double, Double, Int)]]())

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    for (i <- start until end) {
      val x = xSource(i)
      val px = xConv(x)
      val y = ySource(i)
      val py = yConv(y)
      // TODO - include sizing code
      val width = symbolWidth(i)
      val height = symbolHeight(i)
      // TODO - add to connect map
      val color = colorFunction(i)
      r.setColor(color)
      xErrorBars.foreach { ex =>
        val error = ex(i)
        r.setStroke(Renderer.StrokeData(1, Nil))
        r.setColor(BlackARGB)
        r.drawLine(xConv(x - error), y, xConv(x + error), y)
      }
      yErrorBars.foreach { ey =>
        val error = ey(i)
        r.setStroke(Renderer.StrokeData(1, Nil))
        r.setColor(BlackARGB)
        r.drawLine(x, yConv(y - error), x, yConv(y + error))
      }
      (connectWithLines, connectMap).zipped.foreach {
        case ((groupFunc, stroke), cm) =>
          val group = groupFunc(i)
          cm.get(group) match {
            case Some(Nil) => // Shouldn't get here.
            case Some(lst @ ((lastx, lasty, lastc) :: t)) =>
              if (lastc == color) {
                cm(group) ::= (px, py, color)
              } else {
                r.setColor(lastc)
                r.setStroke(stroke)
                r.drawLinePath(lst.map(_._1), lst.map(_._2))
                cm(group) = (px, py, color) :: (lastx, lasty, color) :: Nil
              }
            case None =>
              cm(group) = (px, py, color) :: Nil
          }
      }
      symbol.drawSymbol(px, py, width, height, r)
    }
    (connectWithLines, connectMap).zipped.foreach {
      case ((groupFunc, stroke), cm) =>
        for ((group, lst @ ((_, _, c) :: _)) <- cm) {
          r.setColor(c)
          r.setStroke(stroke)
          r.drawLinePath(lst.map(_._1), lst.map(_._2))
        }
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