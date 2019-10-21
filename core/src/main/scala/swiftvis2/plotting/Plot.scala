package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.ScatterStyle
import swiftvis2.plotting.styles.BarStyle
import swiftvis2.plotting.styles.HistogramStyle
import swiftvis2.plotting.styles.BoxPlotStyle
import swiftvis2.plotting.styles.ViolinPlotStyle
import swiftvis2.plotting.styles.NumberNumberPlotStyle
import swiftvis2.plotting.styles.CategoryNumberPlotStyle
import swiftvis2.plotting.styles.PlotStyle
import swiftvis2.plotting.styles.CategoryCategoryPlotStyle

/**
 * This class represents the full concept of a plot in SwiftVis2. It contains maps of the various
 * types of elements that can go into plots with unique identifying names.
 *
 * Note that both PlotTextData and PlotGridData include Bounds for where the elements should appear.
 * This allows the user to place multiple plots with separate axes or multiple labels. Text is drawn
 * after all plot grids so it will appear on top of them.
 */
case class Plot(texts: Map[String, Plot.TextData] = Map.empty, grids: Map[String, Plot.GridData] = Map.empty, legends: Seq[PlotLegend] = Seq.empty) {
  import Plot._
  
  /**
   * Draws this plot to a renderer scaled to the specified Bounds.
   * @param r The Renderer to draw to.
   * @param bounds The fractional bounding box in that Renderer to draw this plot to.
   */
  def render(r: Renderer, bounds: Bounds) = {
    r.setColor(0xffffffff)
    r.fillRectangle(bounds)
    grids.foreach { case (_, g) => g.grid.render(r, bounds.subXY(g.bounds)) }
    texts.foreach { case (_, t) => t.text.render(r, bounds.subXY(t.bounds)) }
    legends.foreach(x => x.render(r, bounds.subXY(x.bounds)))
    r.finish()
  }
  
  // Fluent Interface

  /**
   * Generates a new Plot with the added TextData.
   */
  def withText(name: String, textData: TextData): Plot = {
    copy(texts = texts + (name -> textData))
  }
  
  /**
   * Generate a new plot with the added text and bounds.
   */
  def withText(name: String, text: PlotText, bounds: Bounds): Plot = {
    copy(texts = texts + (name -> TextData(text, bounds)))
  }
  
  /**
   * Generate a new plot with an updated TextData.
   */
  def updatedText(f: TextData => TextData, name: String = "Title"): Plot = {
    copy(texts = texts + (name -> f(texts(name))))
  }
  
  /**
   * Generate a new plot with an updated grid data.
   */
  def updatedGrid(f: GridData => GridData, gridName: String = "Main"): Plot = {
    copy(grids = grids + (gridName -> f(grids(gridName))))
  }
  
  /**
   * Generate a new plot with an updated bounds for a grid. 
   */
  def updatedGridBounds(f: Bounds => Bounds, gridName: String = "Main"): Plot = {
    copy(grids = grids + (gridName -> grids(gridName).copy(bounds = f(grids(gridName).bounds))))
  }
  
  /**
   * Generate a new plot with an updated axis.
   */
  def updatedAxis[A <: Axis](axisName: String, f: A => A, gridName: String = "Main"): Plot = {
    val grid = grids(gridName).grid
    val axis = grids(gridName).grid.axes(axisName).asInstanceOf[A]
    copy(grids = grids + (gridName -> grids(gridName).copy(grid = grid.copy(axes = grid.axes + (axisName -> f(axis)))))) 
  }

  /**
   * Generate a new plot with an updated plot style.
   */
  def updatedStyle[A <: PlotStyle](f: A => A, gridName: String = "Main", row: Int = 0, col: Int = 0, stack: Int = 0): Plot = {
    val grid = grids(gridName).grid
    copy(grids = grids + (gridName -> grids(gridName).copy(grid = grid.updatedStyle(f, row, col, stack))))
  }
  
  def withAxis(axisName: String, axis: Axis, gridName: String = "Main"): Plot = {
    val grid = grids(gridName).grid
    copy(grids = grids + (gridName -> grids(gridName).copy(grid = grid.withAxis(axisName, axis))))
  }
  
  def withModifiedAxis[A <: Axis](currentAxisName: String, newAxisName: String, f: A => A, gridName: String = "Main"): Plot = {
    val grid = grids(gridName).grid
    copy(grids = grids + (gridName -> grids(gridName).copy(grid = grid.withModifiedAxis(currentAxisName, newAxisName, f))))
  }
  
  def updatedStyleXAxis(axisName: String, row: Int = 0, col: Int = 0, stack: Int = 0, gridName: String = "Main"): Plot = {
    val grid = grids(gridName).grid
    copy(grids = grids + (gridName -> grids(gridName).copy(grid = grid.updatedStyleXAxis(axisName, row, col, stack))))
  }
  
  // TODO - Add methods for updating and adding different elements.
  
  // withRow
  // withColumn
  // withStyle
  
}

/**
 * This contains helper methods that will set up simple 1x1 plot grids with various plot types. Make sure that you import swiftvis2.plotting to
 * get implicit conversions. These implicit conversions allow you to pass Scala sequences, arrays, or plain literals in places where the
 * functions require some type of PlotSeries. Importing swiftvis2.plotting._ also brings is color ARGB declarations.
 */
object Plot {
  /**
   * Combines textual information with a fractional bounds for rendering.
   */
  case class TextData(text: PlotText, bounds: Bounds)

  /**
   * Combines a plot grid with fractional bounds for rendering.
   */
  case class GridData(grid: PlotGrid, bounds: Bounds)

  /**
   * Plots a single style in a 1x1 grid.
   * @param style The plot styles to stack on a 1x1 grid.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param xType The type of the x-axis if it is numeric. This argument isn't used for a categorical x-axis.
   * @param yType The type of the y-axis if it is numeric. This argument isn't used for a categorical y-axis.
   */
  def simple(style: PlotStyle, title: String = "", xLabel: String = "", yLabel: String = "",
      xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    style match {
      case nnps: NumberNumberPlotStyle =>
        val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
        val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, style)
        val (tMap, gMap) = titleAndGridMaps(text, grid)
        Plot(tMap, gMap)
      case cnps: CategoryNumberPlotStyle =>
        val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
        val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
        val yAxis = NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
          Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
        val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
        val (tMap, gMap) = titleAndGridMaps(text, grid)
        Plot(tMap, gMap)
      case ccps: CategoryCategoryPlotStyle => 
        val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
        val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
        val yAxis = CategoryAxis("y", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
        val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
        val (tMap, gMap) = titleAndGridMaps(text, grid)
        Plot(tMap, gMap)
    }
  }

  /**
   * Make a 1x1 grid with with multiple plots that all share the same x and y axis with the ability to add connecting lines and error bars. If you want to
   * plot multiple lines for separate sets of data, this is likely the easiest approach. Note that this requires all plots have the same axis types.
   * @param styles A sequence of the plot styles to stack on a 1x1 grid.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param xType The type of the x-axis if it is numeric.
   * @param yType The type of the y-axis if it is numeric.
   */
  def stacked(styles: Seq[PlotStyle], title: String = "", xLabel: String = "", yLabel: String = "",
                xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    require(styles.nonEmpty, "Stacked plot needs at least one plot in the sequence to work.")
    styles.head match {
      case nnps: NumberNumberPlotStyle =>
        require(styles.forall(_.isInstanceOf[NumberNumberPlotStyle]), "All axis styles much match for plot stack.")
        val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
        val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, styles: _*)
        val (tMap, gMap) = titleAndGridMaps(text, grid)
        Plot(tMap, gMap)
      case cnps: CategoryNumberPlotStyle =>
        require(styles.forall(_.isInstanceOf[CategoryNumberPlotStyle]), "All axis styles much match for plot stack.")
        val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
        val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
        val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
        val yAxis = NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
          Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
        val grid = PlotGrid(Seq(Seq(styles.map(s => Plot2D(s, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
        val (tMap, gMap) = titleAndGridMaps(text, grid)
        Plot(tMap, gMap)
    }
  }
  
  // TODO - add grid
  
  // TODO - add stackedGrid
  
  /**
   * Make a 1x1 grid with with multiple numeric axes plots that all share the same x and y axis with the ability to add connecting lines and error bars. If you want to
   * plot multiple lines for separate sets of data, this is likely the easiest approach.
   * @param styles A sequence of the plot styles to stack on a 1x1 grid.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param xType The type of the x-axis if it is numeric.
   * @param yType The type of the y-axis if it is numeric.
   */
  def stackedNN(styles: Seq[NumberNumberPlotStyle], title: String = "", xLabel: String = "", yLabel: String = "",
                xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, styles: _*)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Make a 1x1 grid with with multiple scatter plots that all share the same x and y axis with the ability to add connecting lines and error bars. If you want to
   * plot multiple lines for separate sets of data, this is likely the easiest approach.
   * @param styles A sequence of the plot styles to stack on a 1x1 grid.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param yType The type of the y-axis if it is numeric.
   */
  def stackedCN(styles: Seq[CategoryNumberPlotStyle], title: String = "", xLabel: String = "", yLabel: String = "",
                yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    val grid = PlotGrid(Seq(Seq(styles.map(s => Plot2D(s, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * This makes an MxN grid of numeric axis plots that all share the same axes.
   * @param styles A 2D grid of plot styles.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param xType The type of the x-axis if it is numeric.
   * @param yType The type of the y-axis if it is numeric.
   */
  def gridNN(styles: Seq[Seq[NumberNumberPlotStyle]], title: String = "", xLabel: String = "", yLabel: String = "",
             xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xAxis = NumericAxis("x", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, xType)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    val plots = styles.map { row =>
      row.map {
        case style =>
          Seq(Plot2D(style, "x", "y"))
      }
    }
    val grid = PlotGrid(plots, Map("x" -> xAxis, "y" -> yAxis), (0 until styles.map(_.length).max).map(_ => 1.0), styles.map(_ => 1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * This makes an MxN grid of scatter plots that all share the same axes.
   * @param styles A 2D grid of plot styles.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param yType The type of the y-axis if it is numeric.
   */
  def gridCN(styles: Seq[Seq[CategoryNumberPlotStyle]], title: String = "", xLabel: String = "", yLabel: String = "",
             yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    val plots = styles.map { row =>
      row.map {
        case style =>
          Seq(Plot2D(style, "x", "y"))
      }
    }
    val grid = PlotGrid(plots, Map("x" -> xAxis, "y" -> yAxis), (0 until styles.map(_.length).max).map(_ => 1.0), styles.map(_ => 1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }
  
  /**
   * This will create a row of styles with a mix of numeric axes and categorical axes.
   * @param styles The sequence of plot styles that you want along the row.
   * @param title The title put on the plot.
   * @param xNumLabel The label drawn on the x-axis of numbered axes.
   * @param xCatLabel The label drawn on the x-axis of categorical axes.
   * @param yLabel The label drawn on the y-axis.
   * @param xType The type of the x-axis if it is numeric. This argument isn't used for a categorical x-axis.
   * @param yType The type of the y-axis if it is numeric.
   */
  def row(styles: Seq[PlotStyle], title: String = "", xNumLabel: String = "", xCatLabel: String = "", yLabel: String = "",
      xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xNumAxis = NumericAxis("nx", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xNumLabel, font)), Axis.DisplaySide.Min, xType)
    val xCatAxis = CategoryAxis("cx", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xCatLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    val plots = Seq(styles.map { 
      case style: NumberNumberPlotStyle =>
          Seq(Plot2D(style, "nx", "y"))
      case style: CategoryNumberPlotStyle =>
          Seq(Plot2D(style, "cx", "y"))
    })
    val grid = PlotGrid(plots, Map("nx" -> xNumAxis, "cx" -> xCatAxis, "y" -> yAxis), styles.map(_ => 1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }
  
  // TODO - add column

  /**
   * This makes an MxN grid of scatter plots that all share the same axes.
   * @param styles A 2D grid of plot styles.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param xType The type of the x-axis if it is numeric. This argument isn't used for a categorical x-axis.
   * @param yType The type of the y-axis if it is numeric. This argument isn't used for a categorical y-axis.
   */
  def stackedGridNN(styles: Seq[Seq[Seq[NumberNumberPlotStyle]]], title: String = "", xLabel: String = "", yLabel: String = "",
             xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xAxis = NumericAxis("x", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, xType)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    val plots = styles.map { row =>
      row.map { stack => 
        stack.map { style =>
          Plot2D(style, "x", "y")
      } } }
    val grid = PlotGrid(plots, Map("x" -> xAxis, "y" -> yAxis), (0 until styles.map(_.length).max).map(_ => 1.0), styles.map(_ => 1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }
  
  /////////////////////////// Older Style Convenience Methods ////////////////////////////////////////////////

  /**
   * Make a basic scatter plot with a single set of data. The different series are treated as parallel arrays. So the ith index
   * in each one matches up. Implicit conversions will allow you to use constants for values such as size and color. You should not
   * use constants for the x and y values.
   * @param x The x values of the data points.
   * @param y The y values of the data points.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param symbolSize The values to use for the sizes of the symbols.
   * @param symbolColor The value to use for the colors of the symbols.
   * @param xSizing The style of sizing for the symbols in the x direction.
   * @param ySizing The style of sizing for the symbols in the y direction.
   */
  def scatterPlot(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "",
                  xLabel: String = "", yLabel: String = "", symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB,
                  xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear,
                  xSizing: PlotSymbol.Sizing.Value = PlotSymbol.Sizing.Pixels, ySizing: PlotSymbol.Sizing.Value = PlotSymbol.Sizing.Pixels): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, xSizing, ySizing, symbolColor, None)
    val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, style)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * This makes a scatter plot where certain points are connected by lines. If you pass a constant for the line grouping, all
   * the points will be connected. If you pass a Int => Double or a Seq[Double], then dots whose indices in x and y evaluated to the
   * same value will be connected.
   * @param x The x values of the data points.
   * @param y The y values of the data points.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param symbolSize The values to use for the sizes of the symbols.
   * @param symbolColor The value to use for the colors of the symbols.
   * @param lineGrouping This series tells what subsets to connect the values to.
   * @param lineStyle The style of lines to connect the points with.
   */
  def scatterPlotWithLines(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "",
                           xLabel: String = "", yLabel: String = "",
                           symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB,
                           lineGrouping: PlotSeries = 0, lineStyle: Renderer.StrokeData = Renderer.StrokeData(1, Nil),
                           xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, symbolColor,
      Some(ScatterStyle.LineData(lineGrouping, lineStyle)))
    val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, style)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Makes a scatter plot where each point has error bars associated with it.
   * @param x The x values of the data points.
   * @param y The y values of the data points.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   * @param symbolSize The values to use for the sizes of the symbols.
   * @param symbolColor The value to use for the colors of the symbols.
   * @param xError The size of the error bars in the x direction.
   * @param yError The size of the error bars in the y direction.
   */
  def scatterPlotWithErrorBars(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "",
                               xLabel: String = "", yLabel: String = "", symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB,
                               xError: PlotDoubleSeries, yError: PlotDoubleSeries, xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, symbolColor,
      None, Some(xError), Some(yError))
    val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, style)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Make a 1x1 grid with with multiple scatter plots that all share the same x and y axis.
   * @param pdata A sequence with information about the different scatter plots. The values in the tuple are x, y, color, and size for each point.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def scatterPlots(pdata: Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries)], title: String = "",
                   xLabel: String = "", yLabel: String = "", xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    scatterPlotsFull(pdata.map(t => (t._1, t._2, t._3, t._4, None, None, None)), title, xLabel, yLabel, xType, yType)
  }

  /**
   * Make a 1x1 grid with with multiple scatter plots that all share the same x and y axis with the ability to add connecting lines and error bars. If you want to
   * plot multiple lines for separate sets of data, this is likely the easiest approach.
   * @param pdata A sequence with information about the different scatter plots. The values in the tuple are x, y, color, size, line value, x-error, and y-error for each point.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def scatterPlotsFull(
    pdata: Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries, Option[ScatterStyle.LineData], Option[PlotDoubleSeries], Option[PlotDoubleSeries])],
    title: String = "", xLabel: String = "", yLabel: String = "",
    xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val styles = for ((x, y, argb, size, lines, xerr, yerr) <- pdata) yield {
      ScatterStyle(x, y, Ellipse, size, size, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, argb, lines, xerr, yerr)
    }
    val grid = PlotGrid.oneByOne(xLabel, xType, yLabel, yType, styles: _*)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * This makes an MxN grid of scatter plots that all share the same axes.
   * @param pdata A 2D grid (sequence of sequence) with information about the different scatter plots. The values in the tuple are x, y, color, and size for each point.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def scatterPlotGrid(pdata: Seq[Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries)]], title: String = "", xLabel: String = "", yLabel: String = "",
                      xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    scatterPlotGridFull(pdata.map(s => s.map(t => (t._1, t._2, t._3, t._4, None, None, None))), title, xLabel, yLabel, xType, yType)
  }

  /**
   * This makes an MxN grid of scatter plots that all share the same axes.
   * @param pdata A 2D grid (sequence of sequence) with information about the different scatter plots. The values in the tuple are x, y, color, size, line value, x-error, and y-error for each point.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def scatterPlotGridFull(
    pdata: Seq[Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries, Option[ScatterStyle.LineData], Option[PlotDoubleSeries], Option[PlotDoubleSeries])]],
    title: String = "", xLabel: String = "", yLabel: String = "", xType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear, yType: Axis.ScaleStyle.Value = Axis.ScaleStyle.Linear): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xAxis = NumericAxis("x", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, xType)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, yType)
    val plots = pdata.map { row =>
      row.map {
        case (x, y, argb, size, lines, xerr, yerr) =>
          Seq(Plot2D(ScatterStyle(x, y, Ellipse, size, size, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, argb, lines, xerr, yerr), "x", "y"))
      }
    }
    val grid = PlotGrid(plots, Map("x" -> xAxis, "y" -> yAxis), (0 until pdata.map(_.length).max).map(_ => 1.0), pdata.map(_ => 1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Make a bar plot with the specified categories using the provides values and colors. The number of elements in the sequences of the first
   * element of the tuples for valsAndColors should match the number of elements in the categories sequence.
   * @param categories The names for the different categories.
   * @param valsAndColors A sequence that includes the values for the bars and the colors to use to draw them.
   * @param stacked Tells if the bars should be stacked instead of side-by-side.
   * @param fracWidth Gives the fraction of the with on the category axis that the bars should take up.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def barPlot(categories: PlotStringSeries, valsAndColors: Seq[BarStyle.DataAndColor], stacked: Boolean = false, fracWidth: Double = 0.8, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val style = BarStyle(categories, valsAndColors, stacked, fracWidth)
    val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Make a simple bar plot with one set of bars where values are associated with categories in a Map.
   * @param data The names for the different categories along with their associated values.
   * @param color The color to draw the bars.
   * @param stacked Tells if the bars should be stacked instead of side-by-side.
   * @param fracWidth Gives the fraction of the with on the category axis that the bars should take up.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def barPlotMap(data: Map[String, Double], color: Int = 0xffff0000, stacked: Boolean = false, fracWidth: Double = 0.8, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val categories = data.keySet.toSeq
    val vac = categories.map(k => BarStyle.DataAndColor(Seq(data(k)), color))
    val style = BarStyle(categories, vac, stacked, fracWidth)
    val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Make a simple histogram. If not centered on bins, the bins series should be one element longer than the values.
   * @param bins A series of values that represent either the centers of bins or the edges of bins.
   * @param vals The values for each of the bins. Note that this isn't the data, but the counts in each bin.
   * @param color The color to draw the histogram boxes in.
   * @param centerOnBins Tells if the bins values are a bin centers or if the define the edges of bins.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def histogramPlot(bins: PlotDoubleSeries, vals: PlotDoubleSeries, color: Int, centerOnBins: Boolean,
                    title: String = "", xLabel: String = "", yLabel: String = "", binsOnX: Boolean = true): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = HistogramStyle(bins, Seq(HistogramStyle.DataAndColor(vals, color)), centerOnBins, binsOnX)
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis("x", if (binsOnX) None else Some(0.0), None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis("y", if (binsOnX) Some(0.0) else None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

/**
   * Make a simple histogram. If not centered on bins, the bins series should be one element longer than the values.
   * @param bins A series of values that represent either the centers of bins or the edges of bins.
   * @param data The data that we are binning to make the Histogram.
   * @param color The color to draw the histogram boxes in.
   * @param centerOnBins Tells if the bins values are a bin centers or if the define the edges of bins.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def histogramPlotFromData(bins: PlotDoubleSeries, data: PlotDoubleSeries, color: Int,
                    title: String = "", xLabel: String = "", yLabel: String = "", binsOnX: Boolean = true): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = HistogramStyle.fromData(data, bins, color, binsOnX)
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis("x", if (binsOnX) None else Some(0.0), None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis("y", if (binsOnX) Some(0.0) else None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }
  
  /**
   * Make a grid of histograms. If not centered on bins, the bins series should be one element longer than the values.
   * All histograms share the same bins and the same x axis. You can determine if they share the Y axis or if each row gets its own.
   * @param bins A series of values that represent either the centers of bins or the edges of bins.
   * @param valsAndColors A 2D structure of the values for each of the bins and their associated colors.
   * @param centerOnBins Tells if the bins values are a bin centers or if the define the edges of bins.
   * @param sharedYAxis Tells if each row in the grid should use the same Y axis.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def histogramGrid(bins: PlotDoubleSeries, valsAndColors: Seq[Seq[HistogramStyle.DataAndColor]], centerOnBins: Boolean, sharedYAxis: Boolean,
                    title: String = "", xLabel: String = "", yLabel: String = "", binsOnX: Boolean = true): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val plots = valsAndColors.zipWithIndex.map {
      case (row, r) =>
        row.map { t => Seq(Plot2D(HistogramStyle(bins, Seq(t), centerOnBins, binsOnX), "x", if (sharedYAxis) "y" else "y"+r)) }
    }
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis("x", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxes = if (sharedYAxis) {
      Seq("y" -> NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear))
    } else {
      valsAndColors.indices.map(r => "y"+r -> NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear))
    }
    val grid = PlotGrid(plots, (("x" -> xAxis) +: yAxes).toMap, (0 until valsAndColors.map(_.length).max).map(_ => 1.0), valsAndColors.map(_ => 1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Make a stacked histogram. If not centered on bins, the bins series should be one element longer than the values.
   * @param bins A series of values that represent either the centers of bins or the edges of bins.
   * @param valsAndColors A sequence with the values and colors for each of the bins.
   * @param centerOnBins Tells if the bins values are a bin centers or if the define the edges of bins.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def stackedHistogramPlot(bins: Seq[Double], valsAndColors: Seq[HistogramStyle.DataAndColor], centerOnBins: Boolean, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = HistogramStyle(bins, valsAndColors, centerOnBins)
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis("x", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis("y", Some(0.0), None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Makes a box plot using the specified categories and corresponding data values.
   * @param categories The names for the different categories.
   * @param plotData A sequence of the series of data whose distributions should be displayed.
   * @param boxWidthFrac The fraction of the category space in the x-axis that each box will take.
   * @param symbol The type of symbol to use for the outliers.
   * @param symbolSize The size of symbol to draw for the outliers.
   * @param color The color to draw the box plot with.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def boxPlot(categories: Array[String], plotData: Array[PlotDoubleSeries], boxWidthFrac: Double = 0.8, symbol: PlotSymbol = EllipseLine, symbolSize: Double = 7.5,
              color: Int = BlackARGB, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val style = BoxPlotStyle(categories, plotData, boxWidthFrac, symbol, symbolSize, color, Renderer.StrokeData(1.0, Nil))
    val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  /**
   * Makes a box plot using the specified categories and corresponding data values.
   * @param categories The names for the different categories.
   * @param plotData A sequence of the series of data whose distributions should be displayed.
   * @param bandwidth An optional bandwidth for the kernel that is used to approximate the distribution. This is primarily needed if the distribution isn't close to normal.
   * @param widthFrac The fraction of the category space in the x-axis that each box will take.
   * @param color The color to draw the box plot with.
   * @param title The title put on the plot.
   * @param xLabel The label drawn on the x-axis.
   * @param yLabel The label drawn on the y-axis.
   */
  def violinPlot(categories: Array[String], plotData: Array[PlotDoubleSeries], bandwidth: Option[Double] = None, widthFrac: Double = 0.8,
                 color: Int = BlackARGB, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val style = ViolinPlotStyle(categories, plotData, widthFrac, color, Renderer.StrokeData(1.0, Nil), bandwidth)
    val xAxis = CategoryAxis("x", Axis.TickStyle.Both, 0.0, font, Some(Axis.NameSettings(xLabel, font)), Axis.DisplaySide.Min)
    val yAxis = NumericAxis("y", None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings(yLabel, font)), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    val (tMap, gMap) = titleAndGridMaps(text, grid)
    Plot(tMap, gMap)
  }

  private def titleAndGridMaps(text: PlotText, grid: PlotGrid): (Map[String, TextData], Map[String, GridData]) = {
    if (text.text.isEmpty) (Map(), Map("Main" -> GridData(grid, Bounds(0, 0, 0.98, 0.98))))
    else (Map("Title" -> TextData(text, Bounds(0, 0, 1.0, 0.1))), Map("Main" -> GridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
}
