package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.ScatterStyle
import swiftvis2.plotting.styles.BarStyle
import swiftvis2.plotting.styles.HistogramStyle

case class Plot(texts: Map[String, Plot.PlotTextData], grids: Map[String, Plot.PlotGridData]) {
  def render(r: Renderer, bounds: Bounds) = {
    r.setColor(0xffffffff)
    r.fillRectangle(bounds)
    grids.foreach { case (_, g) => g.grid.render(r, bounds.subXY(g.bounds)) }
    texts.foreach { case (_, t) => t.text.render(r, bounds.subXY(t.bounds)) }
  }
}

object Plot {
  case class PlotTextData(text: PlotText, bounds: Bounds)
  case class PlotGridData(grid: PlotGrid, bounds: Bounds)
  
  def scatterPlot(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String = "", xLabel: String = "", yLabel: String = "", symbolSize: PlotDoubleSeries = 10, symbolColor: PlotIntSeries = BlackARGB): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, symbolSize, symbolSize, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, symbolColor, None)
    val grid = PlotGrid.oneByOne(xLabel, yLabel, style)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  def scatterPlots(pdata: Seq[(PlotDoubleSeries, PlotDoubleSeries, PlotIntSeries, PlotDoubleSeries)], title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val styles = for((x, y, argb, size) <- pdata) yield {
      ScatterStyle(x, y, Ellipse, size, size, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, argb, None)
    }
    val grid = PlotGrid.oneByOne(xLabel, yLabel, styles:_*)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
  def barPlot(categories: Seq[String], valsAndColors: Seq[(Seq[Double], Int)], stacked: Boolean = false, fracWidth: Double = 0.8, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
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

  def histogramPlot(bins: Seq[Double], valsAndColors: Seq[(Seq[Double], Int)], title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val vac = valsAndColors.map { case (xs, c) =>
      (xs: PlotDoubleSeries, c)
    }
    val style = HistogramStyle(bins, vac)
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(xLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val yAxis = NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
  
//  def stackedHistogramPlot(data: Seq[(Double, Seq[Double])], color: Int = 0xffff0000, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
//    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
//    val text = PlotText(title, 0xff000000, font, Renderer.HorizontalAlign.Center, 0.0)
//    val categories = data.keySet.toSeq
//    val vac = categories.map(k => (Seq(data(k)):PlotDoubleSeries, color))
//    val style = BarStyle(categories, vac, stacked, fracWidth)
//    val xAxis = CategoryAxis(Axis.TickStyle.Both, 0.0, font, Some(xLabel -> font), Axis.DisplaySide.Min)
//    val yAxis = NumericAxis(Some(0.0), None, None, Axis.TickStyle.Both, 
//        Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(yLabel -> font), Axis.DisplaySide.Min, Axis.ScaleStyle.Linear)
//    val grid = PlotGrid(Seq(Seq(Seq(Plot2D(style, "x", "y")))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0), 0.15)
//    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
//        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
//  }


}