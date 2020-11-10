package swiftvis2.plotting.renderer

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.ttf.SDL_ttf._
import sdl2.ttf.Extras._
import swiftvis2.plotting.{Axis, BlackARGB, BlueARGB, Bounds, CategoryAxis, ColorGradient, CyanARGB, Ellipse, GreenARGB, MagentaARGB, NoSymbol, NumericAxis, Plot, Plot2D, PlotDoubleSeries, PlotGrid, PlotIntSeries, PlotLegend, PlotSymbol, PlotText, Rectangle, RedARGB, YellowARGB, WhiteARGB}
import swiftvis2.plotting.Bounds
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles._
import swiftvis2.plotting.{Axis, BlackARGB, BlueARGB, Bounds, CategoryAxis, ColorGradient, CyanARGB, Ellipse, GreenARGB, MagentaARGB, NoSymbol, NumericAxis, Plot, Plot2D, PlotDoubleSeries, PlotGrid, PlotIntSeries, PlotLegend, PlotSymbol, PlotText, Rectangle, RedARGB, YellowARGB, WhiteARGB}
import swiftvis2.plotting.styles.ScatterStyle.LineData
import swiftvis2.plotting.Plot.TextData
import swiftvis2.plotting.Plot.GridData

object HelloWorld extends App {
  val xLabel = "x"
  val yLabel = "y"
  val numberFormat = "%1.1f"
  println("Hello world! [In Scala Native]")
  //swiftRend.drawLine(200, 200, 300, 300)
  //swiftRend.drawRectangle(50, 50, 50, 50)
  //swiftRend.drawEllipse(150, 150, 25, 25)
  //swiftRend.fillRectangle(100, 400, 50, 50)
  val xPnt = 1 to 10
  val yPnt = xPnt.map(a => a * a)
  val cg = (x: Int) => if(x < 5) RedARGB else BlackARGB
  val colors = xPnt.map(cg)
  val sp = Plot.scatterPlot(xPnt, yPnt, title = "Quadratic", xLabel = "", yLabel = "", symbolColor = colors)
  //sp.render(swiftRend, Bounds(0, 0, 800, 800))
  def longForm(): Plot = {
    val font = new Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis1 = new NumericAxis("x1", None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(90, font, numberFormat)),
      Some(Axis.NameSettings("X1", font)))
    val xAxis2 = new NumericAxis("x2", None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(90, font, numberFormat)),
      Some(Axis.NameSettings("X2", font)))
    val xAxisCat = new CategoryAxis("xcat", Axis.TickStyle.Both, 0, font, Some(Axis.NameSettings("Categories", font)), Axis.DisplaySide.Max)
    val yAxis1 = new NumericAxis("y1", None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, numberFormat)),
      Some(Axis.NameSettings("Y1", font)))
    val yAxis2 = new NumericAxis("y2", None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.0f")),
      Some(Axis.NameSettings("Y2", font)))
    val yAxis3 = new NumericAxis("y3", None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.0f")),
      Some(Axis.NameSettings("Y3", font)), Axis.DisplaySide.Max)

    // Main Scatter plot
    val (mainX, mainY) = (for (_ <- 1 to 1000) yield {
      val r = java.lang.Math.random * java.lang.Math.random * java.lang.Math.random
      val theta = java.lang.Math.random * 2 * java.lang.Math.PI
      (r * math.cos(theta), r * math.sin(theta))
    }).unzip
    val mainScatter = ScatterStyle(mainX, mainY, Ellipse, 5, 5, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, BlueARGB)
    val mainScatterPlot = Plot2D(mainScatter, "x1", "y1")

    // Function overplot
    val (funcX, funcY) = (-1.0 to 1.0 by 0.002).map(x => x -> math.sin(20 * x * x) * 0.4).unzip
    val funcScatter = ScatterStyle(funcX, funcY, NoSymbol, 5, 5, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, BlackARGB,
      Some(ScatterStyle.LineData(1, Renderer.StrokeData(2, Seq.empty))))
    val funcScatterPlot = Plot2D(funcScatter, "x1", "y1")

    // Histogram
    val binSize = 0.02
    val bins = (-1.0 to 1.0 by binSize).toArray
    val counts = Array.fill(bins.length - 1)(0)
    for (x <- mainX) counts(((x + 1) / binSize).toInt min counts.length) += 1
    val histogram = HistogramStyle(bins, Seq(HistogramStyle.DataAndColor(counts, RedARGB)), false)
    val histogramPlot = Plot2D(histogram, "x1", "y2")

    // Bar Chart
    import BarStyle._
    val barChart = BarStyle(Seq("FY", "Sophomore", "Junior", "Senior"), Seq(
      DataAndColor(Seq(70, 25, 15, 5), CyanARGB), DataAndColor(Seq(3, 25, 5, 1), MagentaARGB),
      DataAndColor(Seq(0, 5, 35, 2), YellowARGB), DataAndColor(Seq(0, 0, 5, 40), GreenARGB)),
      false, 0.8)
    val barChartPlot = Plot2D(barChart, "xcat", "y3")

    // Second Scatter
    val x2 = Array.fill(100)(java.lang.Math.random)
    val y2 = x2.map(x => java.lang.Math.cos(x * 3) + 0.2 * java.lang.Math.random)
    val ex2 = x2.map(x => 0.1 * java.lang.Math.random)
    val ey2 = x2.map(x => 0.2 * java.lang.Math.random)
    val cg = ColorGradient(-1.0 -> BlackARGB, 0.0 -> BlueARGB, 1.0 -> GreenARGB)
    val errorScatter = ScatterStyle(x2, y2, Rectangle, 5, ey2, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Scaled, cg(y2),
      None, Some(ex2), Some(ey2))
    val errorScatterPlot = Plot2D(errorScatter, "x2", "y1")

    // Combine in a plotx
    val title = new PlotText("Complex Plot", BlackARGB, font, Renderer.HorizontalAlign.Center, 0)
    val grid1 = PlotGrid(
      Seq(Seq(Seq(histogramPlot), Seq(barChartPlot)), Seq(Seq(mainScatterPlot, funcScatterPlot), Seq(errorScatterPlot))),
      Map("x1" -> xAxis1, "x2" -> xAxis2, "xcat" -> xAxisCat, "y1" -> yAxis1, "y2" -> yAxis2, "y3" -> yAxis3),
      Seq(0.7, 0.3), Seq(0.3, 0.7), 0.1)

    Plot(Map("title" -> Plot.TextData(title, Bounds(0, 0, 1.0, 0.1))), Map("grid1" -> Plot.GridData(grid1, Bounds(0, 0.1, 1.0, 0.9))))
  }
  val rend = SDLRenderer(longForm(), 1200, 1000)
  rend.quit()
}
