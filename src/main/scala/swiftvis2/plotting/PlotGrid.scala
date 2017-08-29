package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.PlotStyle

case class PlotGrid(
    plots: Seq[Seq[Plot2D]],
    axes: Map[String, Axis],
    xWeights: Seq[Double],
    yWeights: Seq[Double],
    axisFrac: Double) extends Plottable {

  // TODO - labels and listeners

  def render(r: Renderer, bounds: Bounds) = {
    val xSum = xWeights.sum
    val ySum = yWeights.sum
    val xStarts = xWeights.scanLeft(0.0)(_ + _).map(_ / xSum)
    val yStarts = yWeights.scanLeft(0.0)(_ + _).map(_ / ySum)

    // Figure out which axes are on the min and max sides.
    val minXAxes = collectXAxes(_.displaySide == Axis.DisplaySide.Min).map(_.distinct)
    val maxXAxes = collectXAxes(_.displaySide == Axis.DisplaySide.Max).map(_.distinct)
    val minYAxes = collectYAxes(_.displaySide == Axis.DisplaySide.Min).map(_.distinct)
    val maxYAxes = collectYAxes(_.displaySide == Axis.DisplaySide.Max).map(_.distinct)
    
    val axisSize = axisFrac * (bounds.width min bounds.height)

    // Don't reserve space if there is no axis.
    val drawXAxisSizeMin = axisSize * minXAxes.foldLeft(0)((mlen, axes) => mlen max axes.length)
    val drawXAxisSizeMax = axisSize * maxXAxes.foldLeft(0)((mlen, axes) => mlen max axes.length)
    val drawYAxisSizeMin = axisSize * minYAxes.foldLeft(0)((mlen, axes) => mlen max axes.length)
    val drawYAxisSizeMax = axisSize * maxYAxes.foldLeft(0)((mlen, axes) => mlen max axes.length)
    
    // General Bounds
    val minXAxisBounds = Bounds(bounds.x+drawYAxisSizeMin, bounds.y+bounds.height-drawXAxisSizeMin, bounds.width-drawYAxisSizeMin-drawYAxisSizeMax, drawXAxisSizeMin)
    val maxXAxisBounds = Bounds(bounds.x+drawYAxisSizeMin, bounds.y, bounds.width-drawYAxisSizeMin-drawYAxisSizeMax, drawXAxisSizeMax)
    val minYAxisBounds = Bounds(bounds.x, bounds.y+drawXAxisSizeMax, drawYAxisSizeMin, bounds.height-drawXAxisSizeMin-drawXAxisSizeMax)
    val maxYAxisBounds = Bounds(bounds.x+bounds.width-drawYAxisSizeMax, bounds.y+drawXAxisSizeMax, drawXAxisSizeMax, bounds.height-drawXAxisSizeMin-drawXAxisSizeMax)
    val fullGridBounds = bounds.subXYBorder(drawYAxisSizeMin, drawYAxisSizeMax, drawXAxisSizeMax, drawXAxisSizeMin)

    // Draw grid of plots
    r.setColor(0xff000000)
    val sizesAndAxisRenderers = (for {
      (row, yStart, yEnd) <- (plots, yStarts, yStarts.tail).zipped
      (p2d, xStart, xEnd) <- (row, xStarts, xStarts.tail).zipped
    } yield {
      val axisBounds = Seq(
            minXAxisBounds.subX(xStart, xEnd),
            maxXAxisBounds.subX(xStart, xEnd),
            minYAxisBounds.subY(yStart, yEnd),
            maxYAxisBounds.subY(yStart, yEnd)
          )
      r.save()
      val b = fullGridBounds.subXY(xStart, xEnd, yStart, yEnd)
      r.setStroke(Renderer.StrokeData(1, Nil))
      r.drawRectangle(b)
      r.setClip(b)
      val (tickFontSizes, nameFontSizes, xAxisRender, yAxisRender) = p2d.style.render(r, b, axes(p2d.xAxisName), axes(p2d.yAxisName), axisBounds)
      r.restore()
      (tickFontSizes, nameFontSizes, p2d.xAxisName -> xAxisRender, p2d.yAxisName -> yAxisRender)
    })
    val tickFontSize = sizesAndAxisRenderers.flatMap(_._1).min
    val nameFontSize = sizesAndAxisRenderers.flatMap(_._2).min
    val axisRenderers = sizesAndAxisRenderers.flatMap { case (_, _, xtup, ytup) => Seq(xtup, ytup) }.toMap

    // Draw X axes
    val minXAxisCount = minXAxes.maxBy(_.size).size
    val minXAxisFracHeight = 1.0 / minXAxisCount
    for ((axisSeq, xStart, xEnd) <- (minXAxes, xStarts, xStarts.tail).zipped) {
      val b = minXAxisBounds.subX(xStart, xEnd)
      for (i <- 0 until minXAxisCount; if i < axisSeq.size) {
        axisRenderers(axisSeq(i))(tickFontSize, nameFontSize)
      }
    }
    val maxXAxisCount = minXAxes.maxBy(_.size).size
    val maxXAxisFracHeight = 1.0 / minXAxisCount
    for ((axisSeq, xStart, xEnd) <- (maxXAxes, xStarts, xStarts.tail).zipped) {
      val b = minXAxisBounds.subX(xStart, xEnd)
      for (i <- 0 until maxXAxisCount; if i < axisSeq.size) {
        axisRenderers(axisSeq(i))(tickFontSize, nameFontSize)
      }
    }

    // Draw Y Axes
    val minYAxisCount = minYAxes.maxBy(_.size).size
    val minYAxisFracHeight = 1.0 / minYAxisCount
    for ((axisSeq, yStart, yEnd) <- (minYAxes, yStarts, yStarts.tail).zipped) {
      val b = minYAxisBounds.subY(yStart, yEnd)
      for (i <- 0 until minYAxisCount; if i < axisSeq.size) {
        axisRenderers(axisSeq(i))(tickFontSize, nameFontSize)
      }
    }
    val maxYAxisCount = minYAxes.maxBy(_.size).size
    val maxYAxisFracHeight = 1.0 / minYAxisCount
    for ((axisSeq, yStart, yEnd) <- (maxYAxes, yStarts, yStarts.tail).zipped) {
      val b = minYAxisBounds.subY(yStart, yEnd)
      for (i <- 0 until maxYAxisCount; if i < axisSeq.size) {
        axisRenderers(axisSeq(i))(tickFontSize, nameFontSize)
      }
    }
  }

  def collectXAxes(pred: Axis => Boolean): Seq[Seq[String]] = {
    plots.foldLeft(Seq.fill(plots(0).size)(Seq.empty[String])) { (names, row) =>
      val toAdd = row.map(p2d => axisNameAsListWithCondition(p2d.xAxisName, pred))
      (names, toAdd).zipped.map((a, b) => (b ++: a))
    }
  }

  def collectYAxes(pred: Axis => Boolean): Seq[Seq[String]] = {
    plots.map(row => row.flatMap(p2d => axisNameAsListWithCondition(p2d.yAxisName, pred)))
  }

  def axisNameAsListWithCondition(axisName: String, pred: Axis => Boolean): Seq[String] = {
    if (axes.contains(axisName) && axes(axisName).isDrawn && pred(axes(axisName))) Seq(axisName) else Seq.empty[String]
  }
}

object PlotGrid {
  def oneByOne(style: PlotStyle): PlotGrid = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.TickLabelSettings(90.0, font, "%1.1f")), Some("X axis" -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.TickLabelSettings(0.0, font, "%1.1f")), Some("Y axis" -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    PlotGrid(Seq(Seq(Plot2D(style, "x", "y"))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
  }
}