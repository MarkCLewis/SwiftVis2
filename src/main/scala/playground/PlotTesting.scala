package playground

import scalafx.application.JFXApp
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer

object PlotTesting extends JFXApp {
  // Short form, single data examples
  val xPnt = 1 to 10
  val yPnt = xPnt.map(a => a * a)
  val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  FXRenderer(plot)

  val plot2 = Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random), 
      "Random Points", "x", "y", 2)
  FXRenderer(plot2, 1500, 500)
  
  // Short form, multiple data example
  val plot3 = Plot.scatterPlots(Seq(
        ((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random), 0xffff0000, 5),
        ((1 to 1000).map(_ => 1.0-math.random * math.random), (1 to 1000).map(_ => 1.0-math.random * math.random), 0xff00ff00, 5)
      ), "Colored Points", "x", "y")
  FXRenderer(plot3, 1200, 700)
  
  // Short form bar plot
  val plot4 = Plot.barPlot(Seq("red", "green", "blue"), Seq(Seq(3.0, 7.0, 4.0) -> 0xffeeff00, Seq(2.0, 1.0, 3.0) -> 0xffee00ff), false, 0.8, "Bar Plot", "Colors", "Measure")
  FXRenderer(plot4, 500, 300)
  
  // Long form
  
}