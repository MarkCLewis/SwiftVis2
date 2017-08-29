This project is an updated version of SwiftVis written in Scala with better support for parallelism, support for JavaFX rendering,
and a programming interface.

SwiftVis2 is still in the early stages. I am focusing on adding basic plotting capability with the programming interface currently.
The graphical dataflow interface and other features will follow.

While it is possible to build plots piece by piece, the fact that SwiftVis2 supports a lot of plotting options can make that tedious.
To help with that, facade methods are added that construct frequently used structures. The following code shows how you can use
two facade methods to generate a scatter plot and display it. Note that you need to do this in a class that extends JFXApp.

```scala
import scalafx.application.JFXApp
import swiftvis2.plotting
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
```

Currently scatter plots are the only plotting style that is implemented.