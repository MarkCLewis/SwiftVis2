package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.PlotStyle

import scala.collection.mutable

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
  plots:    Seq[Seq[Seq[Plot2D]]],
  axes:     Map[String, Axis],
  xWeights: Seq[Double],
  yWeights: Seq[Double],
  axisFrac: Double                = 0.15) extends Plottable {

  if (yWeights.length != plots.length) println(s"Warning!!! Rows in plots not matched by yWeights. ${plots.length} != ${yWeights.length}")
  if (plots.nonEmpty && xWeights.length != plots.head.length) println(s"Warning!!! Columns in plots not matched by xWeights. ${plots.head.length} != ${xWeights.length}")
  
  {
    val xAxes = plots.flatMap(_.flatMap(_.flatMap(_.xAxisName)))
    val yAxes = plots.flatMap(_.flatMap(_.flatMap(_.yAxisName)))
    if ((xAxes ++ yAxes).toSet != axes.keySet) println(s"Warning!!! Used axes don't match provided axes.\nxAxes = $xAxes\nyAxes = $yAxes\naxis keys = ${axes.keys}")
  }

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
    val minXAxisBounds = Bounds(bounds.x + drawYAxisSizeMin, bounds.y + bounds.height - drawXAxisSizeMin, bounds.width - drawYAxisSizeMin - drawYAxisSizeMax, drawXAxisSizeMin)
    val maxXAxisBounds = Bounds(bounds.x + drawYAxisSizeMin, bounds.y, bounds.width - drawYAxisSizeMin - drawYAxisSizeMax, drawXAxisSizeMax)
    val minYAxisBounds = Bounds(bounds.x, bounds.y + drawXAxisSizeMax, drawYAxisSizeMin, bounds.height - drawXAxisSizeMin - drawXAxisSizeMax)
    val maxYAxisBounds = Bounds(bounds.x + bounds.width - drawYAxisSizeMax, bounds.y + drawXAxisSizeMax, drawYAxisSizeMax, bounds.height - drawXAxisSizeMin - drawXAxisSizeMax)
    val fullGridBounds = bounds.subXYBorder(drawYAxisSizeMin, drawYAxisSizeMax, drawXAxisSizeMax, drawXAxisSizeMin)

    val xminhm = new mutable.HashMap[Axis, Double]()
    val xmaxhm = new mutable.HashMap[Axis, Double]()
    val yminhm = new mutable.HashMap[Axis, Double]()
    val ymaxhm = new mutable.HashMap[Axis, Double]()

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
        maxYAxisBounds.subY(yStart, yEnd))
      r.save()
      val b = fullGridBounds.subXY(xStart, xEnd, yStart, yEnd)
      r.setStroke(Renderer.StrokeData(1, Nil))
      r.drawRectangle(b)
      r.setClip(b)
      val axisData = for (p2d <- p2ds) yield {
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
    minXAxes.zipWithIndex.foldLeft(None: Option[Bounds]) {
      case (aggBounds, (axisSeq, index)) =>
        //      for (i <- 0 until minXAxisCount; if i < axisSeq.size) {
        if (axisSeq.nonEmpty) {
          val nextAxis = if (index >= minXAxes.length - 1 || minXAxes(index + 1).isEmpty) None else Some(axes(minXAxes(index + 1)(0)))
          axisRenderers(axisSeq(0) -> index)(tickFontSize, nameFontSize, aggBounds, nextAxis)
        } else None
    }
    val maxXAxisCount = maxXAxes.maxBy(_.size).size
    val maxXAxisFracHeight = 1.0 / maxXAxisCount
    maxXAxes.zipWithIndex.foldLeft(None: Option[Bounds]) {
      case (aggBounds, (axisSeq, index)) =>
        //      for (i <- 0 until maxXAxisCount; if i < axisSeq._1.size) {
        if (axisSeq.nonEmpty) {
          val nextAxis = if (index >= maxXAxes.length - 1 || maxXAxes(index + 1).isEmpty) None else Some(axes(maxXAxes(index + 1)(0)))
          axisRenderers(axisSeq(0) -> index)(tickFontSize, nameFontSize, aggBounds, nextAxis)
        } else None
    }

    // Draw Y Axes
    val minYAxisCount = minYAxes.maxBy(_.size).size
    val minYAxisFracHeight = 1.0 / minYAxisCount
    minYAxes.zipWithIndex.foldLeft(None: Option[Bounds]) {
      case (aggBounds, (axisSeq, index)) =>
        //      for (i <- 0 until minYAxisCount; if i < axisSeq._1.size) {
        if (axisSeq.nonEmpty) {
          val nextAxis = if (index >= minYAxes.length - 1 || minYAxes(index + 1).isEmpty) None else Some(axes(minYAxes(index + 1)(0)))
          axisRenderers(axisSeq(0) -> index)(tickFontSize, nameFontSize, aggBounds, nextAxis)
        } else None
    }
    val maxYAxisCount = maxYAxes.maxBy(_.size).size
    val maxYAxisFracHeight = 1.0 / maxYAxisCount
    maxYAxes.zipWithIndex.foldLeft(None: Option[Bounds]) {
      case (aggBounds, (axisSeq, index)) =>
        //      for (i <- 0 until maxYAxisCount; if i < axisSeq._1.size) {
        if (axisSeq.nonEmpty) {
          val nextAxis = if (index >= maxYAxes.length - 1 || maxYAxes(index + 1).isEmpty) None else Some(axes(maxYAxes(index + 1)(0)))
          axisRenderers(axisSeq(0) -> index)(tickFontSize, nameFontSize, aggBounds, nextAxis)
        } else None
    }
  }

  // Fluent Interface

  def withRow(row: Int = plots.length): PlotGrid = ??? // TODO
  
  def withColumn(col: Int = plots.head.length): PlotGrid = ??? // TODO
  
  def withStyle(style: PlotStyle, row: Int = 0, col: Int = 0, stack: Int = 0): PlotGrid = {
    val prow = plots(row)
    val pcell = prow(col)
    val p2d = pcell(stack)
    copy(plots = plots.updated(row, prow.updated(col, pcell.patch(stack, Seq(p2d.copy(style = style)), 0))))
  }
  
  def updatedStyle[A <: PlotStyle](f: A => A, row: Int = 0, col: Int = 0, stack: Int = 0): PlotGrid = {
    val prow = plots(row)
    val pcell = prow(col)
    val p2d = pcell(stack)
    val newStyle = f(p2d.style.asInstanceOf[A])
    copy(plots = plots.updated(row, prow.updated(col, pcell.updated(stack, p2d.copy(style = newStyle)))))
  }
  
  def withAxis(axisName: String, axis: Axis): PlotGrid = {
    copy(axes = axes + (axisName -> axis))
  }
  
  def withModifiedAxis[A <: Axis](currentAxisName: String, newAxisName: String, f: A => A): PlotGrid = {
    copy(axes = axes + (newAxisName -> f(axes(currentAxisName).asInstanceOf[A])))
  }
  
  def updatedStyleXAxis(axisName: String, row: Int = 0, col: Int = 0, stack: Int = 0): PlotGrid = {
    val prow = plots(row)
    val pcell = prow(col)
    val p2d = pcell(stack)
    copy(plots = plots.updated(row, prow.updated(col, pcell.updated(stack, p2d.copy(xAxisName = axisName)))))
  }

  // Private Methods

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

  private def extremeAxisFunction(hm: mutable.HashMap[Axis, Double], nameFunc: Plot2D => String, styleFunc: PlotStyle => Option[Double], combine: Seq[Double] => Double)(axis: Axis): Double = {
    if (hm.contains(axis)) hm(axis) else {
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
      val ret = if (extr.isEmpty) 0.0 else combine(extr)
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
  def oneByOne(xLabel: String, xType: Axis.ScaleStyle.Value, yLabel: String, yType: Axis.ScaleStyle.Value, styles: PlotStyle*): PlotGrid = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis("x", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, xType)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    PlotGrid(Seq(Seq(styles.map(s => Plot2D(s, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
  }
}