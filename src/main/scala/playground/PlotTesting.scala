package playground

import scalafx.application.JFXApp
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer
import swiftvis2.plotting.ColorGradient

object PlotTesting extends JFXApp {
  // Short form, single data examples
  val xPnt = 1 to 10
  val yPnt = xPnt.map(a => a * a)
  val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  FXRenderer(plot)

  val plot2 = Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random), 
      "Random Points", "Independent", "Dependent", 2)
  FXRenderer(plot2, 1500, 500)
  
  // Short form, multiple data example
  val plot3 = Plot.scatterPlots(Seq(
        ((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random), 0xffff0000, 5),
        ((1 to 1000).map(_ => 1.0-math.random * math.random), (1 to 1000).map(_ => 1.0-math.random * math.random), 0xff00ff00, 5)
      ), "Colored Points", "Horizontal", "Vertical")
  FXRenderer(plot3, 1200, 700)
  
  // Short form, function with color and size
  val xs = 0.0 to 10.0 by 0.01
  val cg = ColorGradient((0.0, 0xffff0000), (5.0, 0xff00ff00), (10.0, 0xff0000ff))
  val plot4 = Plot.scatterPlot(xs, xs.map(math.cos), "Cosine", "Theta", "Value", xs.map(x => math.sin(x)+2), xs.map(cg))
  FXRenderer(plot4, 1500, 500)
  
  // Short form bar plot
  val plot5 = Plot.barPlot(Seq("red", "green", "blue"), Seq(Seq(3.0, 7.0, 4.0) -> 0xffeeff00, Seq(2.0, 1.0, 3.0) -> 0xffee00ff), false, 0.8, "Bar Plot", "Colors", "Measure")
  FXRenderer(plot5, 500, 300)
  
  // Long form
  
}