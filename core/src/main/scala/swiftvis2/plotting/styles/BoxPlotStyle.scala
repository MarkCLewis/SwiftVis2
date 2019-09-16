package swiftvis2.plotting.styles

import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer

/**
 * This holds the data for rendering a box for a single category.
 */
final case class BoxPlotData(category: String, min: Double, firstQuartile: Double, median: Double, thirdQuartile: Double, max: Double, outliers: IndexedSeq[Double])

/**
 * This is the style for drawing a box plot.
 */
final case class BoxPlotStyle(
  boxData:      Seq[BoxPlotData],
  boxWidthFrac: Double,
  symbol:       PlotSymbol,
  symbolSize:   Double,
  color:        Int,
  stroke:       Renderer.StrokeData) extends CategoryNumberPlotStyle {

  private val minData = boxData.map { bpd =>
    bpd.min min (if(bpd.outliers.isEmpty) Double.MaxValue else bpd.outliers.min)
  }.min

  private val maxData = boxData.map { bpd =>
    bpd.max max (if(bpd.outliers.isEmpty) Double.MinValue else bpd.outliers.max)
  }.max

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
             yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[CategoryAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = (0, boxData.length)
    val categories = boxData.map(_.category)

    val (catConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(categories, Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    r.setColor(color)
    r.setStroke(stroke)
    for (i <- start until end) {
      val (scatx, ecatx) = catConv(categories(i))
      val sx = scatx + (1.0 - boxWidthFrac) / 2 * (ecatx - scatx)
      val ex = ecatx - (1.0 - boxWidthFrac) / 2 * (ecatx - scatx)
      val zeroy = yConv(0.0)
      val bd = boxData(i)
      val ymin = yConv(bd.min)
      val ymax = yConv(bd.max)
      val ymedian = yConv(bd.median)
      val y25 = yConv(bd.firstQuartile)
      val y75 = yConv(bd.thirdQuartile)
      val cx = (sx + ex) * 0.5
      r.drawRectangle(sx, ymedian min y25, ex - sx, (ymedian - y25).abs)
      r.drawRectangle(sx, ymedian min y75, ex - sx, (ymedian - y75).abs)
      r.drawLine(cx, ymin, cx, y25)
      r.drawLine(cx, ymax, cx, y75)
      r.drawLine(sx+(ex-sx)/4, ymin, ex-(ex-sx)/4, ymin)
      r.drawLine(sx+(ex-sx)/4, ymax, ex-(ex-sx)/4, ymax)
      for (oy <- bd.outliers) {
        val py = yConv(oy)
        symbol.drawSymbol(cx, py, symbolSize, symbolSize, r)
      }
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }

  def xDataMin(): Option[Double] = None

  def xDataMax(): Option[Double] = None

  def yDataMin(): Option[Double] = Some(minData-0.02*(maxData-minData))

  def yDataMax(): Option[Double] = Some(maxData+0.02*(maxData-minData))
}

object BoxPlotStyle {
  def apply(categories: Seq[String], plotData: Array[PlotDoubleSeries], boxWidthFrac: Double = 0.8, symbol: PlotSymbol = EllipseLine, symbolSize: Double = 5,
            color: Int = BlackARGB, stroke: Renderer.StrokeData = Renderer.StrokeData(1, Nil)): BoxPlotStyle = {
    val boxData = for ((cat, data) <- categories zip plotData) yield {
      val d = (data.minIndex until data.maxIndex).map(data).sorted
      val median = if (d.length % 2 == 1) d(d.length / 2) else 0.5 * (d(d.length / 2) + d(d.length / 2 - 1))
      val firstQuartile = d(d.length / 4)
      val thirdQuartile = d(3 * d.length / 4)
      val maxDiff = thirdQuartile - firstQuartile
      val min = d.head max firstQuartile - 1.5 * maxDiff
      val max = d.last min thirdQuartile + 1.5 * maxDiff
      val outliers = d.filter(x => x < min || x > max)
      BoxPlotData(cat, min, firstQuartile, median, thirdQuartile, max, outliers)
    }
    new BoxPlotStyle(boxData, boxWidthFrac, symbol, symbolSize, color, stroke)
  }
}