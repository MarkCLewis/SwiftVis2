package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.PlotStyle
import java.util.concurrent.ConcurrentHashMap

/**
 * This is the primary plotting element in SwiftVis2. It represents a grid of plots with axes that can surround the edges.
 * Each region in the PlotGrid can hold multiple plots so the data for the plots is given by a Seq[Seq[Seq[Plot2D]]]. The
 * first index is for the row, the second is the column, and the third is a stack of plots drawn at that location. The axes
 * associated with plots are in the Plot2D type by name, which should match up with the Map[String, Axis] passed into this.
 * 
 * Each row and column can be a different size. The sizes are determined by the xWeights and yWeights arguments. Larger values
 * get more space. The amount of space given to each row/column is determined by its contribution to the sum of the weights. If
 * all the weights are the same, the rows/columns will be uniform in size.
 * 
 * The axisFrac argument tells what fraction of the plotting region should be given to the axes. The actual space given to the
 * axes is this fraction times the smaller value of the width or height. Font sizes for axis labels are adjusted to fit whatever
 * size is given here.
 */
case class PlotGrid(
    plots: Seq[Seq[Seq[Plot2D]]],
    axes: Map[String, Axis],
    xWeights: Seq[Double],
    yWeights: Seq[Double],
    axisFrac: Double) extends Plottable {

  // TODO - labels and listeners

  /**
   * Renders this grid to the specified Renderer inside of the specified bounds.
   * @param r The renderer to draw the plot to.
   * @param bounds The bounds the grid should be rendered to.
   */
  def render(r: Renderer, bounds: Bounds): Unit = {
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
    
    import scala.collection.JavaConverters._
    val xminhm = new ConcurrentHashMap[Axis, Double]()
    val xmaxhm = new ConcurrentHashMap[Axis, Double]()
    val yminhm = new ConcurrentHashMap[Axis, Double]()
    val ymaxhm = new ConcurrentHashMap[Axis, Double]()

    // Draw grid of plots
    r.setColor(0xff000000)
    val sizesAndAxisRenderers = (for {
      ((row, i), yStart, yEnd) <- (plots.zipWithIndex, yStarts, yStarts.tail).zipped
      ((p2ds, j), xStart, xEnd) <- (row.zipWithIndex, xStarts, xStarts.tail).zipped
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
      val axisData = for(p2d <- p2ds) yield {
        val (tickFontSizes, nameFontSizes, xAxisRender, yAxisRender) = p2d.style.render(r, b, 
            axes(p2d.xAxisName), axis => extremeAxisFunction(xminhm, p => p.xAxisName, st => st.xDataMin(), _.min)(axis), 
            axis => extremeAxisFunction(xmaxhm, p => p.xAxisName, st => st.xDataMax(), _.max)(axis), 
            axes(p2d.yAxisName), axis => extremeAxisFunction(yminhm, p => p.yAxisName, st => st.yDataMin(), _.min)(axis), 
            axis => extremeAxisFunction(ymaxhm, p => p.yAxisName, st => st.yDataMax(), _.max)(axis),
            axisBounds)
        (tickFontSizes, nameFontSizes, (p2d.xAxisName, j) -> xAxisRender, (p2d.yAxisName, i) -> yAxisRender)
      }
      r.restore()
      axisData
    }).flatten
    val nameFontSize = sizesAndAxisRenderers.flatMap(_._2).min
    val tickFontSize = sizesAndAxisRenderers.flatMap(_._1).min min nameFontSize
    val axisRenderers = sizesAndAxisRenderers.flatMap { case (_, _, xtup, ytup) => Seq(xtup, ytup) }.toMap

    // Draw X axes
    val minXAxisCount = minXAxes.maxBy(_.size).size
    val minXAxisFracHeight = 1.0 / minXAxisCount
    for ((axisSeq, xStart, xEnd) <- (minXAxes.zipWithIndex, xStarts, xStarts.tail).zipped) {
      val b = minXAxisBounds.subX(xStart, xEnd)
      for (i <- 0 until minXAxisCount; if i < axisSeq._1.size) {
        axisRenderers(axisSeq._1(i) -> axisSeq._2)(tickFontSize, nameFontSize)
      }
    }
    val maxXAxisCount = minXAxes.maxBy(_.size).size
    val maxXAxisFracHeight = 1.0 / minXAxisCount
    for ((axisSeq, xStart, xEnd) <- (maxXAxes.zipWithIndex, xStarts, xStarts.tail).zipped) {
      val b = minXAxisBounds.subX(xStart, xEnd)
      for (i <- 0 until maxXAxisCount; if i < axisSeq._1.size) {
        axisRenderers(axisSeq._1(i) -> axisSeq._2)(tickFontSize, nameFontSize)
      }
    }

    // Draw Y Axes
    val minYAxisCount = minYAxes.maxBy(_.size).size
    val minYAxisFracHeight = 1.0 / minYAxisCount
    for ((axisSeq, yStart, yEnd) <- (minYAxes.zipWithIndex, yStarts, yStarts.tail).zipped) {
      val b = minYAxisBounds.subY(yStart, yEnd)
      for (i <- 0 until minYAxisCount; if i < axisSeq._1.size) {
        axisRenderers(axisSeq._1(i) -> axisSeq._2)(tickFontSize, nameFontSize)
      }
    }
    val maxYAxisCount = minYAxes.maxBy(_.size).size
    val maxYAxisFracHeight = 1.0 / minYAxisCount
    for ((axisSeq, yStart, yEnd) <- (maxYAxes.zipWithIndex, yStarts, yStarts.tail).zipped) {
      val b = minYAxisBounds.subY(yStart, yEnd)
      for (i <- 0 until maxYAxisCount; if i < axisSeq._1.size) {
        axisRenderers(axisSeq._1(i) -> axisSeq._2)(tickFontSize, nameFontSize)
      }
    }
  }

  private def collectXAxes(pred: Axis => Boolean): Seq[Seq[String]] = {
    plots.foldLeft(Seq.fill(plots(0).size)(Seq.empty[String])) { (names, row) =>
      val toAdd = row.map(_.flatMap(p2d => axisNameAsListWithCondition(p2d.xAxisName, pred)))
      (names, toAdd).zipped.map((a, b) => (b ++: a))
    }
  }

  private def collectYAxes(pred: Axis => Boolean): Seq[Seq[String]] = {
    plots.map(row => row.flatMap(_.flatMap(p2d => axisNameAsListWithCondition(p2d.yAxisName, pred))))
  }

  private def axisNameAsListWithCondition(axisName: String, pred: Axis => Boolean): Seq[String] = {
    if (axes.contains(axisName) && axes(axisName).isDrawn && pred(axes(axisName))) Seq(axisName) else Seq.empty[String]
  }
  
  private def extremeAxisFunction(hm: ConcurrentHashMap[Axis, Double], nameFunc: Plot2D => String, styleFunc: PlotStyle => Option[Double], combine: Seq[Double] => Double)(axis: Axis): Double = {
    if(hm.contains(axis)) hm.get(axis) else {
      val extr = for {
        row <- plots
        cell <- row
        p <- cell
        if axes(nameFunc(p)) == axis
        ex = styleFunc(p.style)
        x <- ex
      } yield {
        x
      }
      val ret = if(extr.isEmpty) 0.0 else combine(extr)
      hm.put(axis, ret)
      ret
    }
  }
}

/**
 * Contains helper methods for building different types of grids with default properties.
 */
object PlotGrid {
  /**
   * This creates a 1x1 grid with the provided axis labels that has a stack of plot styles.
   */
  def oneByOne(xLabel: String, yLabel: String, styles: PlotStyle*): PlotGrid = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(xLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    PlotGrid(Seq(Seq(styles.map(s => Plot2D(s, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
  }
}