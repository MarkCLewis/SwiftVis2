package swiftvis2.plotting.styles

import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.PlotSymbol
import swiftvis2.plotting.renderer.Renderer.FontData

/**
 * This class represents a highly functional scatter plot. The points can use different symbols that are scaled/sized in different ways
 * in both the X and Y direction. Each point can have a different color associated with it. They can also be connected with lines and
 * have error bars on them.
 */
final case class ScatterStyle(
                               xSource: PlotDoubleSeries,
                               ySource: PlotDoubleSeries,
                               symbol: PlotSymbol = Ellipse,
                               symbolWidth: PlotDoubleSeries = 10,
                               symbolHeight: PlotDoubleSeries = 10,
                               xSizing: PlotSymbol.Sizing.Value = PlotSymbol.Sizing.Pixels,
                               ySizing: PlotSymbol.Sizing.Value = PlotSymbol.Sizing.Pixels,
                               colors: PlotIntSeries = BlackARGB,
                               lines: Option[ScatterStyle.LineData] = None,
                               xErrorBars: Option[PlotDoubleSeries] = None,
                               yErrorBars: Option[PlotDoubleSeries] = None,
                               uniformCols: Boolean = true,
                               colorDesc: String = "",
                               colorDescs: Seq[(String, Int)] = Seq.empty,
                               sizingDesc: String = "",
                               colorIsGradient: Boolean = false,
                               legendSizeTrans: Double => Double = identity,
                               gradient: Option[ColorGradient] = None) extends NumberNumberPlotStyle {

  import ScatterStyle._
  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
             yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[NumericAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()

    val connectMap = lines.map(_ => collection.mutable.Map[Any, List[(Double, Double, Int)]]())

    val (xConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(bounds.x, bounds.x + bounds.width,
      xminFunc(xNAxis), xmaxFunc(xNAxis), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
// Commented lines allow parallel calculation of plot information. Currently this doesn't help performance.
//    val pdata = for (i <- (start until end).par) yield {
    for (i <- start until end) {
      val x = xSource(i)
      val y = ySource(i)
      val width = symbolWidth(i)
      val height = symbolHeight(i)
      val (pminx, pmaxx) = PlotSymbol.sizing(xSizing, x, width, xConv, bounds.width)
      val (pminy, pmaxy) = PlotSymbol.sizing(ySizing, y, height, yConv, bounds.height)
      val px = (pminx+pmaxx)/2
      val py = (pminy+pmaxy)/2
      val pwidth = pmaxx-pminx
      val pheight = pmaxy-pminy
      val color = colors(i)
//      ScatterData(i, x, y, px, py, pwidth, pheight, color)
//    }
//    for(ScatterData(i, x, y, px, py, pwidth, pheight, color) <- pdata.seq) {
      r.setColor(color)
      xErrorBars.foreach { ex =>
        val error = ex(i)
        r.setStroke(Renderer.StrokeData(1, Nil))
        r.setColor(BlackARGB)
        r.drawLine(xConv(x - error), py, xConv(x + error), py)
      }
      yErrorBars.foreach { ey =>
        val error = ey(i)
        r.setStroke(Renderer.StrokeData(1, Nil))
        r.setColor(BlackARGB)
        r.drawLine(px, yConv(y - error), px, yConv(y + error))
      }
      (lines, connectMap).zipped.foreach {
        case (ScatterStyle.LineData(groupFunc, stroke), cm) =>
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
      symbol.drawSymbol(px, py, pwidth, pheight, r)
    }
    (lines, connectMap).zipped.foreach {
      case (LineData(groupFunc, stroke), cm) =>
        for ((group, lst @ ((_, _, c) :: _)) <- cm) {
          r.setColor(c)
          r.setStroke(stroke)
          r.drawLinePath(lst.map(_._1), lst.map(_._2))
        }
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }

  def calcStartEnd(): (Int, Int) = {
    (Array(xSource, ySource, symbolWidth, symbolHeight, colors, lines.map(_.groups).getOrElse(UnboundDoubleSeries)).map(_.minIndex).max,
      Array(xSource, ySource, symbolWidth, symbolHeight, colors, lines.map(_.groups).getOrElse(UnboundDoubleSeries)).map(_.maxIndex).min)
  }

  def xDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMin(start, end))
  }
  def xdMin(start: Int, end: Int): Double = (start until end).foldLeft(Double.MaxValue)((d, a) => d min xSource(a)-xErrorBars.map(_(a)).getOrElse(0.0))

  def xDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(xdMax(start, end))
  }
  def xdMax(start: Int, end: Int): Double = (start until end).foldLeft(Double.MinValue)((d, a) => d max xSource(a)+xErrorBars.map(_(a)).getOrElse(0.0))

  def yDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMin(start, end))
  }
  def ydMin(start: Int, end: Int): Double = (start until end).foldLeft(Double.MaxValue)((d, a) => d min ySource(a)-yErrorBars.map(_(a)).getOrElse(0.0))

  def yDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMax(start, end))
  }
  def ydMax(start: Int, end: Int): Double = (start until end).foldLeft(Double.MinValue)((d, a) => d max ySource(a)+yErrorBars.map(_(a)).getOrElse(0.0))

  def coloredBy(determinant: PlotIntSeries = colors, descs: Seq[(String, Int)]): ScatterStyle = this.copy(colorDescs = descs, colors = determinant)

  def sizedBy(desc: String, xsz: PlotDoubleSeries = symbolWidth, ysz: PlotDoubleSeries = symbolHeight, trans: Double => Double = identity): ScatterStyle = {
    this.copy(sizingDesc = desc, symbolWidth = xsz, symbolHeight = ysz, legendSizeTrans = trans)
  }

  def legendFields: Seq[LegendItem] = {

    val sizingSample = if(sizingDesc.isEmpty) Seq.empty else {
      val wSeq = (symbolWidth.minIndex until symbolWidth.maxIndex).map(symbolWidth(_)).distinct.sorted
      val hSeq = (symbolHeight.minIndex until symbolHeight.maxIndex).map(symbolHeight(_)).distinct.sorted
      Seq(wSeq.head -> hSeq.head, wSeq(wSeq.length / 2) -> hSeq(hSeq.length / 2), wSeq.last -> hSeq.last)
    }

    val sizingItem: LegendItem = LegendItem(sizingDesc, Seq.empty, sizingSample.map { case (w, h) =>
      val x = legendSizeTrans(w)
      val desc = legendSizeTrans(w).toString ++ (if(w != h) "," ++ legendSizeTrans(h).toString else "")
      val illu = Illustration(BlackARGB, Some(symbol), w, h)
      LegendItem(desc, Seq(illu), Seq.empty)
    })

    val colorPoints = if(uniformCols && colorDesc.nonEmpty) {
      Seq(Illustration(colors(0), Some(symbol), Illustration.med, Illustration.med))
    } else if (colorDesc.isEmpty) Seq.empty else {
      val colSeq = (for (i <- colors.minIndex until colors.maxIndex) yield colors(i)).sorted

      val startCol = colSeq(0)
      val midCol = colSeq(colSeq.distinct.length / 2)
      val endCol = colSeq.last

      Seq(
        Illustration(startCol, Some(symbol), Illustration.med, Illustration.med),
        Illustration(midCol, Some(symbol), Illustration.med, Illustration.med),
        Illustration(endCol, Some(symbol), Illustration.med, Illustration.med)
      )
    }
    val realColorItem = Seq(LegendItem(colorDesc, Seq(Illustration(0, Some(Rectangle), 100, 100, gradient))))
    val colorItems = if(colorIsGradient) realColorItem else {
      for(desc <- colorDescs) yield {
        LegendItem(desc._1, Seq(Illustration(symbol = Some(symbol), width = Illustration.med, height = Illustration.med, color = desc._2)))
      }
    }


    colorItems :+ sizingItem
  }

  def coloredByGradient(determinant: ColorGradient, data: PlotDoubleSeries, desc: String = "", uniform: Boolean = true): ScatterStyle = this.copy(colorDesc = desc, colors = determinant(data), uniformCols = uniform, colorIsGradient = true, gradient = Some(determinant))
}

object ScatterStyle {
  /**
   * This class provides the information on how to connect elements with lines. The groups is a series that has one element
   * for each data point. Elements with the same value in this series are connected with line. If you provide a constant,
   * all the data points will be connected with one line.
   */
  case class LineData(groups: PlotSeries, stroke: Renderer.StrokeData = Renderer.StrokeData(1))

  val connectAll = Some(LineData(0))

  private case class ScatterData(i: Int, d: Double, y: Double, px: Double, py: Double, pwidth: Double, pheight: Double, color: Int)
}