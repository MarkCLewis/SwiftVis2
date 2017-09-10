package playground

import scalafx.application.JFXApp
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer
import swiftvis2.plotting.ColorGradient

object PlotTesting extends JFXApp {
  // Short form, single data examples
  def scatter1(): Unit = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
    FXRenderer(plot)
  }

  def scatter2(): Unit = {
    val plot = Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random),
      "Random Points", "Independent", "Dependent", 2)
    FXRenderer(plot, 1500, 500)
  }

  // Short form, multiple data example
  def scatterMultidata(): Unit = {
    val plot = Plot.scatterPlots(Seq(
      ((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random*0.5), 0xffff0000, 5),
      ((1 to 1000).map(_ => 1.0 - math.random * math.random), (1 to 1000).map(_ => 1.0 - math.random * math.random*0.5), 0xff00ff00, 5)), "Colored Points", "Horizontal", "Vertical")
    FXRenderer(plot, 1200, 700)
  }

  // Short form, function with color and size
  def scatterWithSizeandColor(): Unit = {
    val xs = 0.0 to 10.0 by 0.01
    val cg = ColorGradient((0.0, 0xffff0000), (5.0, 0xff00ff00), (10.0, 0xff0000ff))
    val plot = Plot.scatterPlot(xs, xs.map(math.cos), "Cosine", "Theta", "Value", xs.map(x => math.sin(x) + 2), xs.map(cg))
    FXRenderer(plot, 1500, 500)
  }

  // Short form bar plot
  def barChart(): Unit = {
    val plot = Plot.barPlot(Seq("red", "green", "blue"), Seq(Seq(3.0, 7.0, 4.0) -> 0xffeeff00, Seq(2.0, 1.0, 3.0) -> 0xffee00ff), true, 0.8, "Bar Plot", "Colors", "Measure")
    FXRenderer(plot, 500, 300)
  }

  // Short form histogram plot
  def histogram(): Unit = {
    val bins = 1.0 to 10.1 by 1.0
    val plot = Plot.histogramPlot(bins, Seq(bins.map(12 - _) -> 0xff0000FF), "Histogram Plot", "Colors", "Measure")
    FXRenderer(plot, 500, 300)
  }

  // Short form histogram plot
  def histogram2(): Unit = {
    val bins = 1.0 to 10.1 by 1.0
    val plot = Plot.histogramPlot(bins, Seq(bins.map(12 - _) -> 0xff0000ff, bins.map(x => 5*(math.cos(x)+2)) -> 0xffff0000), "Histogram Plot", "Colors", "Measure")
    FXRenderer(plot, 500, 300)
  }

  // Long form
  def longForm(): Unit = {
    
  }

//  scatterMultidata()
//  barChart()
  histogram2()
}