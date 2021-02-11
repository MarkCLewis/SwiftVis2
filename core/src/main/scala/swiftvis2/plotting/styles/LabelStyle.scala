package swiftvis2.plotting.styles

import swiftvis2.plotting.Axis.AxisRenderer
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.renderer.Renderer.{FontData, StrokeData}
import swiftvis2.plotting.{Axis, BlackARGB, Bounds, LegendItem, NumericAxis, PlotDoubleSeries, PlotIntSeries, PlotSymbol, WhiteARGB}

final case class LabelStyle(
                            textXSource: PlotDoubleSeries = Seq.empty[Double],
                            textYSource: PlotDoubleSeries = Seq.empty[Double],
                            symbolXSource: PlotDoubleSeries = Seq.empty[Double],
                            symbolYSource: PlotDoubleSeries = Seq.empty[Double],
                            symbols: Seq[PlotSymbol] = Seq.empty,
                            texts: Seq[String] = Seq.empty,
                            font: FontData = FontData("Arial", Renderer.FontStyle.Plain),
                            symbolColors: PlotIntSeries = Seq.empty[Int],
                            textColors: PlotIntSeries = Seq.empty[Int],
                            symbolWidth: PlotDoubleSeries = Seq.empty[Double],
                            symbolHeight: PlotDoubleSeries = Seq.empty[Double],
                            textWidth: PlotDoubleSeries = Seq.empty[Double],
                            textHeight: PlotDoubleSeries = Seq.empty[Double],
                            textXNudges: PlotDoubleSeries = Seq.empty[Double],
                            textYNudges: PlotDoubleSeries = Seq.empty[Double],
                            symbolXNudges: PlotDoubleSeries = Seq.empty[Double],
                            symbolYNudges: PlotDoubleSeries = Seq.empty[Double],
                            boxedText: Boolean = false,
                            lineStartPointsX: Seq[Double] = Seq.empty,
                            lineStartPointsY: Seq[Double] = Seq.empty,
                            lineEndPointsX: Seq[Double] = Seq.empty,
                            lineEndPointsY: Seq[Double] = Seq.empty,
                            lineThicknesses: PlotDoubleSeries = Seq.empty[Double],
                            lineColors: PlotIntSeries = Seq.empty[Int]) extends NumberNumberPlotStyle {
  /**
   * This method will render the plot to the specified region using the provided renderer with the given axes and bounds for the axes.
   *
   * @param r          This is the renderer that plot will be drawn to.
   * @param bounds     This is the region of the renderer that will draw to.
   * @param xAxis      This is the x-axis for the style. Code should call renderInfo and pass through data.
   * @param yAxis      This is the y-axis for the style. Code should call renderInfo and pass through data.
   * @param axisBounds These are passed in by the grid and used by the axes. You shouldn't use them for other purposes.
   * @return This is all information provided by the renderInfo methods of the axes that is returned to the grid.
   */
  override def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double,
                      xmaxFunc: Axis => Double, yAxis: Axis, yminFunc: Axis => Double,
                      ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], AxisRenderer, AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    val (start, end) = calcTextStartEnd
    for(i <- start until end) {
      if(texts.nonEmpty) {
        val txt = texts(i)
        val (x, y) = (textXSource(i), textYSource(i))
        val col = textColors(i)
        val (width, height) = (textWidth(i), textHeight(i))
        val fs = r.maxFontSize(Seq(txt), width, height, font)
        val drawX = xConv(x) + textXNudges(i)
        val drawY = yConv(y) + textYNudges(i)
        if (boxedText) {
          r.setColor(WhiteARGB)
          r.fillRectangle(drawX - width * 0.5, drawY - height * 0.5, width, height)
          r.setColor(BlackARGB)
          r.drawRectangle(drawX - width * 0.5, drawY - height * 0.5, width, height)
        }
        r.setFont(font, fs)
        r.setColor(col)
        r.drawText(txt, drawX, drawY, Renderer.HorizontalAlign.Center, 0.0)
      }

      if(symbols.nonEmpty) {
        val sym = symbols(i)
        val (x, y) = (symbolXSource(i), symbolYSource(i))
        val col = symbolColors(i)
        val (width, height) = (symbolWidth(i), symbolHeight(i))
        val drawX = xConv(x) + symbolXNudges(i)
        val drawY = yConv(y) + symbolYNudges(i)
        r.setColor(col)
        sym.drawSymbol(drawX, drawY, width, height, r)
      }
    }

    if(lineStartPointsX.nonEmpty) {
      for (i <- lineStartPointsX.indices) {
        val (startX, startY) = lineStartPointsX(i) -> lineStartPointsY(i)
        val (endX, endY) = lineEndPointsX(i) -> lineEndPointsY(i)
        val thickness = lineThicknesses(i)
        val lineCol = lineColors(i)
        val (drawStartX, drawStartY) = xConv(startX) -> yConv(startY)
        val (drawEndX, drawEndY) = xConv(endX) -> yConv(endY)
        r.setColor(lineCol)
        r.setStroke(StrokeData(thickness))
        r.drawLine(drawStartX, drawStartY, drawEndX, drawEndY)
      }
    }

    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }
  def calcTextStartEnd: (Int, Int) = (Array(textXSource, textYSource, textColors).map(_.minIndex).max,
    Array(textXSource, textYSource, textColors).map(_.maxIndex).min)


  /**
   * Returns the minimum numeric value for the x-axis for this plot if applicable.
   */
  override def xDataMin(): Option[Double] = None

  /**
   * Returns the maximum numeric value for the x-axis for this plot if applicable.
   */
  override def xDataMax(): Option[Double] = None

  /**
   * Returns the minimum numeric value for the y-axis for this plot if applicable.
   */
  override def yDataMin(): Option[Double] = None

  /**
   * Returns the maximum numeric value for the y-axis for this plot if applicable.
   */
  override def yDataMax(): Option[Double] = None

  /**
   * Returns the sequence of legend entries currently associated with this plot. If none are available, returns Seq.empty.
   */
  override def legendFields: Seq[LegendItem] = Seq.empty
}