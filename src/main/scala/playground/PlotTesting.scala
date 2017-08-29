package playground

import scalafx.application.JFXApp
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer

object PlotTesting extends JFXApp {
  val xPnt = (1.0 to 10.0 by 1.0).toSeq
  val yPnt = xPnt.map(a => a * a)
  val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  FXRenderer(plot)

  val plot2 = Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random), "Random Points", "x", "y")
  FXRenderer(plot2, 1500, 500)
}