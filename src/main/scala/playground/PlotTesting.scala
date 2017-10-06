package playground

import scalafx.application.JFXApp
import swiftvis2.plotting
import swiftvis2.plotting._
import swiftvis2.plotting.renderer._
import swiftvis2.plotting.styles.ScatterStyle
import swiftvis2.plotting.styles.HistogramStyle
import swiftvis2.plotting.styles.BarStyle
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File


object PlotTesting extends JFXApp {
  /**
   * Short form, single data examples
   */
  def scatter1(): Plot = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  }

  /**
   * Basic scatter plot with a large number of random points.
   */
  def scatter2(): Plot = {
    Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random),
      "Random Points", "Independent", "Dependent", 2)
  }

  /**
   * Scatter plots with points connected by lines.
   */
  def scatterLines(): Plot = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    Plot.scatterPlotWithLines(xPnt, yPnt, "Quadratic", "x", "y", lineGrouping = 1)
  }
  
  /**
   * Scatter plot with error bars on the points.
   */
  def scatterWithErrorBars(): Plot = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    Plot.scatterPlotWithErrorBars(xPnt, yPnt, "Quadratic", "x", "y", 5, BlackARGB, xPnt.map(_ * 0.2), yPnt.map(_ * 0.3))
  }
  
  /**
   * Short form, multiple data example
   */
  def scatterMultidata(): Plot = {
    Plot.scatterPlots(Seq(
      ((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random*0.5), RedARGB, 5),
      ((1 to 1000).map(_ => 1.0 - math.random * math.random), (1 to 1000).map(_ => 1.0 - math.random * math.random*0.5), GreenARGB, 5)), "Colored Points", "Horizontal", "Vertical")
  }
  
  def scatterGrid(): Plot = {
    val x1 = (1 to 1000).map(_ => math.random)
    val y1 = x1.map(_ * math.random)
    val x2 = 0.0 to 1.1 by 0.01
    val y2 = x2.map(x => math.cos(10*x*x))
    val x3 = (1 to 1000).map(_ => math.random*math.random)
    val y3 = x2.map(x => math.sin(10*x*x))
    val c3 = x2.map(_ => math.random)
    val x4 = 0.01 to 1.1 by 0.01
    val y4 = x4.map(x => 0.01/x)
    val cg = ColorGradient(0.0 -> BlackARGB, 0.5 -> RedARGB, 1.0 -> WhiteARGB)
    
    Plot.scatterPlotGrid(
        Seq(Seq((x1, y1, BlackARGB, 5), (x2, y2, BlueARGB, 5)),
            Seq((x3, y3, c3.map(cg), 10), (x4, y4, GreenARGB, 5))),
        "Plot Grid", "Shared X", "Shared Y")
  }

  /**
   * Short form, function with color and size
   */
  def scatterWithSizeandColor(): Plot = {
    val xs = 0.0 to 10.0 by 0.01
    val cg = ColorGradient((0.0, RedARGB), (5.0, GreenARGB), (10.0, BlueARGB))
    Plot.scatterPlot(xs, xs.map(math.cos), "Cosine", "Theta", "Value", xs.map(x => math.sin(x) + 2), xs.map(cg))
  }

  /**
   * Short form bar plot
   */
  def barChart(): Plot = {
    Plot.barPlot(Seq("red", "green", "blue"), Seq(Seq(3.0, 7.0, 4.0) -> YellowARGB, Seq(2.0, 1.0, 3.0) -> MagentaARGB), true, 0.8, "Bar Plot", "Colors", "Measure")
  }

  /**
   * Short form histogram plot
   */
  def histogram(): Plot = {
    val bins = 0.0 to 10.0 by 1.0
    Plot.histogramPlot(bins, bins.map(12 - _).init, BlueARGB, false, "Histogram Plot", "Value", "Count")
  }

  /**
   * Short form histogram plot
   */
  def histogram2(): Plot = {
    val bins = 1.0 to 10.1 by 1.0
    Plot.stackedHistogramPlot(bins, Seq(bins.map(12 - _) -> BlueARGB, bins.map(x => 5*(math.cos(x)+2)) -> 0xffff0000), true, "Histogram Plot", "Value", "Count")
  }

  /**
   * Short form grid of histogram plots
   */
  def histogramGrid(): Plot = {
    val bins = 0.0 to 10.0 by 1.0
    Plot.histogramGrid(bins, Seq(
        Seq((bins.map(12 - _).init:PlotDoubleSeries) -> RedARGB, (bins.map(1 + _).init:PlotDoubleSeries) -> GreenARGB),
        Seq((bins.map(c => c*c/6).tail:PlotDoubleSeries) -> BlueARGB, (bins.map(c => 10*math.random).init:PlotDoubleSeries) -> CyanARGB)), 
        false, false, "Histogram Grid", "Values", "Counts")
  }

  /**
   * Long form - this example shows the general capabilities of the plot grid and adding multiple plots 
   */
  def longForm(): Plot = {
    val font = new Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val xAxis1 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(90, font, "%1.1f")), Some("X1" -> font))
    val xAxis2 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(90, font, "%1.1f")), Some("X2" -> font))
    val xAxisCat = new CategoryAxis(Axis.TickStyle.Both, 0, font, Some("Categories" -> font), Axis.DisplaySide.Max)
    val yAxis1 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.1f")), Some("Y1" -> font))
    val yAxis2 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.0f")), Some("Y2" -> font))
    val yAxis3 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.0f")), Some("Y3" -> font), Axis.DisplaySide.Max)
    
    // Main Scatter plot
    val (mainX, mainY) = (for(_ <- 1 to 1000) yield {
      val r = math.random*math.random*math.random
      val theta = math.random*2*math.Pi
      (r*math.cos(theta), r*math.sin(theta))
    }).unzip
    val mainScatter = ScatterStyle(mainX, mainY, Ellipse, 5, 5, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, BlueARGB)
    val mainScatterPlot = Plot2D(mainScatter, "x1", "y1")
    
    // Function overplot
    val (funcX, funcY) = (-1.0 to 1.0 by 0.002).map(x => x -> math.sin(20*x*x)*0.4).unzip 
    val funcScatter = ScatterStyle(funcX, funcY, NoSymbol, 5, 5, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, BlackARGB, 
        Some((1:PlotSeries) -> Renderer.StrokeData(2, Seq.empty)))
    val funcScatterPlot = Plot2D(funcScatter, "x1", "y1")
    
    // Histogram
    val binSize = 0.02
    val bins = (-1.0 to 1.0 by binSize).toArray
    val counts = Array.fill(bins.length-1)(0)
    for(x <- mainX) counts(((x+1)/binSize).toInt min counts.length) += 1
    val histogram = HistogramStyle(bins, Seq((counts:PlotDoubleSeries) -> RedARGB), false)
    val histogramPlot = Plot2D(histogram, "x1", "y2")
    
    // Bar Chart
    val barChart = BarStyle(Seq("FY", "Sophomore", "Junior", "Senior"), Seq((Seq(70, 25, 15, 5):PlotDoubleSeries) -> CyanARGB, 
        (Seq(3, 25, 5, 1):PlotDoubleSeries) -> MagentaARGB, (Seq(0, 5, 35, 2):PlotDoubleSeries) -> YellowARGB, (Seq(0, 0, 5, 40):PlotDoubleSeries) -> GreenARGB),
        false, 0.8)
    val barChartPlot = Plot2D(barChart, "xcat", "y3")
    
    // Second Scatter
    val x2 = Array.fill(100)(math.random)
    val y2 = x2.map(x => math.cos(x*3)+0.2*math.random)
    val ex2 = x2.map(x => 0.1*math.random)
    val ey2 = x2.map(x => 0.2*math.random)
    val cg = ColorGradient(-1.0 -> BlackARGB, 0.0 -> BlueARGB, 1.0 -> GreenARGB)
    val errorScatter = ScatterStyle(x2, y2, Rectangle, 5, ey2, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Scaled, y2.map(cg), 
        None, Some(ex2), Some(ey2))
    val errorScatterPlot = Plot2D(errorScatter, "x2", "y1")
    
    // Combine in a plot
    val title = new PlotText("Complex Plot", BlackARGB, font, Renderer.HorizontalAlign.Center, 0)
    val grid1 = PlotGrid(Seq(Seq(Seq(histogramPlot), Seq(barChartPlot)), Seq(Seq(mainScatterPlot, funcScatterPlot), Seq(errorScatterPlot))),
        Map("x1" -> xAxis1, "x2" -> xAxis2, "xcat" -> xAxisCat, "y1" -> yAxis1, "y2" -> yAxis2, "y3" -> yAxis3),
        Seq(0.7, 0.3), Seq(0.3, 0.7), 0.1)
    
    Plot(Map("title" -> Plot.PlotTextData(title, Bounds(0, 0, 1.0, 0.1))), Map("grid1" -> Plot.PlotGridData(grid1, Bounds(0, 0.1, 1.0, 0.9))))
  }
  
  def saveToFile(): Unit = {
    val plot = Plot.scatterPlots(Seq(
      ((1 to 10000).map(_ => math.random * math.random), (1 to 10000).map(_ => math.random * math.random*0.5), RedARGB, 5),
      ((1 to 10000).map(_ => 1.0 - math.random * math.random), (1 to 10000).map(_ => 1.0 - math.random * math.random*0.5), GreenARGB, 5)), "Colored Points", "Horizontal", "Vertical")
    FXRenderer.saveToImage(plot, 1200, 700, new File("sample.png"))
  }

  
  Future {
//    scatter1()
//    scatter2()
//    scatterLines()
//    scatterGrid()
//    scatterWithErrorBars()
//    scatterMultidata()
//    scatterWithSizeandColor()
//    barChart()
//    histogram()
//    histogram2()
//    histogramGrid()
    SVGRenderer(longForm(), "plot.svg", 1200, 1000)
    FXRenderer(longForm(), 1200, 1000)
//    saveToFile()
  }
}