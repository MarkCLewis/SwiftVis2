package swiftvis2.plotting.styles

import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer

sealed trait DensityTreeNode {
  def density(x: Double, bandwidth: Double): Double
}
final case class InternalDTN(min: Double, max: Double, mean: Double, cnt: Int, left: DensityTreeNode, right: DensityTreeNode) extends DensityTreeNode {
  def density(x: Double, bandwidth: Double): Double = {
    if (max - min < bandwidth / 4) {
      val dx = mean - x
      cnt * math.exp(-dx * dx / (2 * bandwidth * bandwidth))
    } else {
      left.density(x, bandwidth) + right.density(x, bandwidth)
    }
  }
}
final case class LeafDTN(values: IndexedSeq[Double], mean: Double) extends DensityTreeNode {
  def density(x: Double, bandwidth: Double): Double = {
    if (values.last - values.head < bandwidth / 4) {
      val dx = mean - x
      values.length * math.exp(-dx * dx / (2 * bandwidth * bandwidth))
    } else {
      values.foldLeft(0.0) { (sum, xi) =>
        val dx = x - xi
        sum + math.exp(-dx * dx / (2 * bandwidth * bandwidth))
      }
    }
  }
}

/**
 * This holds the data for rendering a box for a single category.
 */
final case class ViolinPlotData(category: String, min: Double, firstQuartile: Double, median: Double, thirdQuartile: Double, max: Double, densities: IndexedSeq[(Double, Double)])

/**
 * A plot style of drawing violin plots.
 */
final case class ViolinPlotStyle private (
  violinData:   Seq[ViolinPlotData],
  maxWidthFrac: Double,
  color:        Int,
  stroke:       Renderer.StrokeData,
  maxDensity:   Double) extends CategoryNumberPlotStyle {

  private val minData = violinData.map(_.min).min

  private val maxData = violinData.map(_.max).max

  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
             yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[CategoryAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = (0, violinData.length)
    val categories = violinData.map(_.category)

    val (catConv, xtfs, xnfs, xRender) = xNAxis.renderInfo(categories, Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y + bounds.height, bounds.y,
      yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    r.setColor(color)
    r.setStroke(stroke)
    for (i <- start until end) {
      val (scatx, ecatx) = catConv(categories(i))
      val sx = scatx + (1.0 - maxWidthFrac) / 2 * (ecatx - scatx)
      val ex = ecatx - (1.0 - maxWidthFrac) / 2 * (ecatx - scatx)
      val vd = violinData(i)
      val cx = (sx + ex) * 0.5
      var lastY = yConv(vd.densities.head._1)
      var lastFrac = vd.densities.head._2.toDouble / maxDensity
      val hwidth = (ex - sx) * 0.5
      r.drawLine(cx - lastFrac * hwidth, lastY, cx + lastFrac * hwidth, lastY)
      val markers = Array(vd.firstQuartile, vd.median, vd.thirdQuartile)
      var curMark = 0
      for ((oy, cnt) <- vd.densities.tail) {
        val py = yConv(oy)
        val frac = cnt.toDouble / maxDensity
        r.drawLine(cx - lastFrac * hwidth, lastY, cx - frac * hwidth, py)
        r.drawLine(cx + lastFrac * hwidth, lastY, cx + frac * hwidth, py)
        if (curMark < markers.length && oy >= markers(curMark)) {
          r.drawLine(cx - 0.5*(frac+lastFrac) * hwidth, 0.5*(py+lastY), cx + 0.5*(frac+lastFrac) * hwidth, 0.5*(py+lastY))
          curMark += 1
        }
        lastY = py
        lastFrac = frac
      }
      r.drawLine(cx - lastFrac * hwidth, lastY, cx + lastFrac * hwidth, lastY)
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }

  def xDataMin(): Option[Double] = None

  def xDataMax(): Option[Double] = None

  def yDataMin(): Option[Double] = Some(minData - 0.1 * (maxData - minData))

  def yDataMax(): Option[Double] = Some(maxData + 0.1 * (maxData - minData))
}

object ViolinPlotStyle {
  def apply(categories: Seq[String], plotData: Array[PlotDoubleSeries], maxWidthFrac: Double = 0.8,
            color: Int = BlackARGB, stroke: Renderer.StrokeData = Renderer.StrokeData(1, Nil), bandwidth: Option[Double] = None): ViolinPlotStyle = {
    var maxDensity = 0.0
    val violinData = for ((cat, data) <- categories zip plotData) yield {
      val d = (data.minIndex until data.maxIndex).map(data).sorted
      val min = d.head
      val max = d.last
      val median = if (d.length % 2 == 1) d(d.length / 2) else 0.5 * (d(d.length / 2) + d(d.length / 2 - 1))
      val firstQuartile = d(d.length / 4)
      val thirdQuartile = d(3 * d.length / 4)
      val maxDiff = thirdQuartile - firstQuartile
      val bw = bandwidth.getOrElse {
        val mean = d.sum / d.length
        val std = math.sqrt(d.foldLeft(0.0)((sum, x) => sum + (x - mean) * (x - mean)) / (d.length - 1))
        math.pow(4 * std * std * std * std * std / (3 * d.length), 0.2) // Rule-of-thumb bandwidth estimator
      }
      val tree = buildTree(d, bw, 0, d.length)
      val densities = (min-0.15*(max-min) to max+0.15*(max-min) by bw / 4).map { x =>
        x -> tree.density(x, bw)
      }
      maxDensity = maxDensity max densities.maxBy(_._2)._2
      ViolinPlotData(cat, min, firstQuartile, median, thirdQuartile, max, densities)
    }
    new ViolinPlotStyle(violinData, maxWidthFrac, color, stroke, maxDensity)
  }

  private def buildTree(xs: IndexedSeq[Double], bandwidth: Double, start: Int, end: Int): DensityTreeNode = {
    if (end - start < 3 || xs(end - 1) - xs(start) < bandwidth / 4) {
      val d = xs.slice(start, end)
      LeafDTN(d, d.sum / d.length)
    } else {
      val mid = (start + end) / 2
      InternalDTN(xs(start), xs(end - 1), xs.slice(start, end).sum / (end - start), end - start, buildTree(xs, bandwidth, start, mid), buildTree(xs, bandwidth, mid, end))
    }
  }
}