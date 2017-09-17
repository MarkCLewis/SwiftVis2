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

case class BarStyle(
    categories: PlotStringSeries,
    valSourceColor: Seq[(PlotDoubleSeries, Int)],
    stacked: Boolean,
    barWidthFrac: Double
    ) extends PlotStyle {
  
  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double,
      yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer) = {
    val xNAxis = xAxis.asInstanceOf[CategoryAxis]
    val yNAxis = yAxis.asInstanceOf[NumericAxis]
    val (start, end) = calcStartEnd()
    
    val (catConv, xtfs, xnfs, xRender) = xNAxis.renderInfo((start until end).map(categories), Axis.RenderOrientation.XAxis, r, axisBounds)
    val (yConv, ytfs, ynfs, yRender) = yNAxis.renderInfo(bounds.y+bounds.height, bounds.y,
        yminFunc(yNAxis), ymaxFunc(yNAxis), Axis.RenderOrientation.YAxis, r, axisBounds)
    for(i <- start until end) {
      val (scatx, ecatx) = catConv(categories(i))
      val ys = valSourceColor.map(vs => vs._1(i))
      val sx = scatx+(1.0-barWidthFrac)/2*(ecatx-scatx)
      val ex = ecatx-(1.0-barWidthFrac)/2*(ecatx-scatx)
      if(stacked) {
        var lasty = 0.0
        for(j <- ys.indices) {
          r.setColor(valSourceColor(j)._2)
          val y = ys(j)+lasty
          val clasty = yConv(lasty)
          val cy = yConv(y)
          r.fillRectangle(sx, clasty min cy, ex-sx, (clasty-cy).abs)
          lasty = y
        }
      } else {
        val zeroy = yConv(0.0)
        val barWidth = (ex-sx)/ys.length
        for(j <- ys.indices) {
          r.setColor(valSourceColor(j)._2)
          val y = yConv(ys(j))
          r.fillRectangle(sx+barWidth*j, y min zeroy, barWidth, (y-zeroy).abs)
        }
      }
    }
    (Seq(xtfs, ytfs), Seq(xnfs, ynfs), xRender, yRender)
  }
  
  def calcStartEnd(): (Int, Int) = {
    ((valSourceColor.map(_._1) :+ categories).map(_.minIndex).max,
     (valSourceColor.map(_._1) :+ categories).map(_.maxIndex).min)
  }

  def xDataMin(): Option[Double] = None
  
  def xDataMax(): Option[Double] = None
    
  def yDataMin(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMin(start, end))
  }
  def ydMin(start: Int, end: Int): Double = {
    if(stacked) {
      (start until end).foldLeft(Double.MaxValue)((d, a) => d min valSourceColor.map(_._1(a)).sum)
    } else {
      (start until end).foldLeft(Double.MaxValue)((d, a) => d min valSourceColor.map(_._1(a)).min)
    }
  }
  
  def yDataMax(): Option[Double] = {
    val (start, end) = calcStartEnd()
    Some(ydMax(start, end))
  }
  def ydMax(start: Int, end: Int): Double = {
    if(stacked) {
      (start until end).foldLeft(Double.MinValue)((d, a) => d max valSourceColor.map(_._1(a)).sum)
    } else {
      (start until end).foldLeft(Double.MinValue)((d, a) => d max valSourceColor.map(_._1(a)).max)
    }
  }
}