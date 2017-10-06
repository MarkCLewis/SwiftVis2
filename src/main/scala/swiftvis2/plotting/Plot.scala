package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.ScatterStyle
import swiftvis2.plotting.styles.BarStyle
import swiftvis2.plotting.styles.HistogramStyle

/**
 * This class represents the full concept of a plot in SwiftVis2. It contains maps of the various
 * types of elements that can go into plots with unique identifying names.
 * 
 * Note that both PlotTextData and PlotGridData include Bounds for where the elements should appear.
 * This allows the user to place multiple plots with separate axes or multiple labels. Text is drawn
 * after all plot grids so it will appear on top of them.
 */
case class Plot(texts: Map[String, Plot.PlotTextData], grids: Map[String, Plot.PlotGridData]) {
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
  }
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
  case class PlotTextData(text: PlotText, bounds: Bounds)
  
  /**
   * Combines a plot grid with fractional bounds for rendering.
   */
  case class PlotGridData(grid: PlotGrid, bounds: Bounds)
  
  /**
   * Make a basic scatter plot with a single set of data.
   */
  def scatterPlot(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "", xLabel: String = "", yLabel: String = "", 
      symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, symbolColor, None)
    val grid = PlotGrid.oneByOne(xLabel, yLabel, style)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  /**
   * This makes a scatter plot where certain points are connected by lines. If you pass a constant for the line grouping, all
   * the points will be connected. If you pass a Int => Double or a Seq[Double], then dots whose indices in x and y evaluated to the
   * same value will be connected.
   */
  def scatterPlotWithLines(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "", xLabel: String = "", yLabel: String = "", 
      symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB, lineGrouping: PlotDoubleSeries): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, symbolColor, 
        Some(lineGrouping -> Renderer.StrokeData(1, Nil)))
    val grid = PlotGrid.oneByOne(xLabel, yLabel, style)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }

  /**
   * Makes a scatter plot where each point has error bars associated with it.
   */
  def scatterPlotWithErrorBars(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "", xLabel: String = "", yLabel: String = "", 
      symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB, xError: PlotDoubleSeries, yError: PlotDoubleSeries): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, symbolColor, 
        None, Some(xError), Some(yError))
    val grid = PlotGrid.oneByOne(xLabel, yLabel, style)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }

  /**
   * Make a 1x1 grid with with multiple scatter plots that all share the same x and y axis.
   */
  def scatterPlots(pdata: Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries)], title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val styles = for((x, y, argb, size) <- pdata) yield {
      ScatterStyle(x, y, Ellipse, size, size, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, argb, None)
    }
    val grid = PlotGrid.oneByOne(xLabel, yLabel, styles:_*)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  /**
   * This makes an MxN grid of scatter plots that all share the same axes.
   */
  def scatterPlotGrid(pdata: Seq[Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries)]], title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(xLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val plots = pdata.map { row =>
      row.map { case (x, y, argb, size) =>
        Seq(Plot2D(ScatterStyle(x, y, Ellipse, size, size, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, argb, None), "x", "y"))
      }
    }
    val grid = PlotGrid(plots, Map("x" -> xAxis, "y" -> yAxis), (0 until pdata.map(_.length).max).map(_ => 1.0), pdata.map(_ => 1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  /**
   * Make a bar plot with the specified categories using the provides values and colors. The number of elements in the sequences of the first
   * element of the tuples for valsAndColors should match the number of elements in the categories sequence.
   */
  def barPlot(categories: PlotStringSeries, valsAndColors: Seq[(Seq[Double], Int)], stacked: Boolean = false, fracWidth: Double = 0.8, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val vac = valsAndColors.map { case (xs, c) =>
      (xs: PlotDoubleSeries, c)
    }
    val style = BarStyle(categories, vac, stacked, fracWidth)
    val xAxis = CategoryAxis(Axis.TickStyle.Both, 0.0, font, Some(xLabel -> font), Axis.DisplaySide.Min)
    val yAxis = NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  /**
   * Make a simple bar plot with one set of bars where values are associated with categories in a Map.
   */
  def barPlotMap(data: Map[String, Double], color: Int = 0xffff0000, stacked: Boolean = false, fracWidth: Double = 0.8, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
    val categories = data.keySet.toSeq
    val vac = categories.map(k => (Seq(data(k)):PlotDoubleSeries, color))
    val style = BarStyle(categories, vac, stacked, fracWidth)
    val xAxis = CategoryAxis(Axis.TickStyle.Both, 0.0, font, Some(xLabel -> font), Axis.DisplaySide.Min)
    val yAxis = NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }

  /**
   * Make a simple histogram. If not centered on bins, the bins series should be one element longer than the values.
   */
  def histogramPlot(bins: PlotDoubleSeries, vals: PlotDoubleSeries, color: Int, centerOnBins: Boolean, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = HistogramStyle(bins, Seq(vals -> color), centerOnBins)
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(xLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  /**
   * Make a grid of histograms. If not centered on bins, the bins series should be one element longer than the values.
   * All histograms share the same bins and the same x axis. You can determine if they share the Y axis or if each row gets its own.
   */
  def histogramGrid(bins: PlotDoubleSeries, vals: Seq[Seq[(PlotDoubleSeries, Int)]], centerOnBins: Boolean, sharedYAxis: Boolean, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val plots = vals.zipWithIndex.map { case (row, r) =>
      row.map { t => Seq(Plot2D(HistogramStyle(bins, Seq(t), centerOnBins), "x", if(sharedYAxis) "y" else "y"+r)) }
    }
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(xLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxes = if(sharedYAxis) { 
      Seq("y" -> NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear))
    } else {
      vals.indices.map(r => "y"+r -> NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear))
    }
    val grid = PlotGrid(plots, (("x" -> xAxis) +: yAxes).toMap, (0 until vals.map(_.length).max).map(_ => 1.0), vals.map(_ => 1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  /**
   * Make a stacked histogram. If not centered on bins, the bins series should be one element longer than the values.
   */
  def stackedHistogramPlot(bins: Seq[Double], valsAndColors: Seq[(Seq[Double], Int)], centerOnBins: Boolean, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val vac = valsAndColors.map { case (xs, c) =>
      (xs: PlotDoubleSeries, c)
    }
    val style = HistogramStyle(bins, vac, centerOnBins)
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(xLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  


}