This project is an updated version of SwiftVis written in Scala with better support for parallelism, support for JavaFX rendering,
and a programming interface.

SwiftVis2 is still in the early stages. I am focusing on adding basic plotting capability with the programming interface currently.
The graphical dataflow interface and other features will follow.

SwiftVis2 is not yet at the point where it belongs in a Maven repository. Until SwiftVis2 is stable enough to put in a Maven repository 
you can use it by compiling and packaging this project and putting the JAR file in the `lib` directory of your sbt project. 

While it is possible to build plots piece by piece, the fact that SwiftVis2 supports a lot of plotting options can make that tedious.
To help with that, facade methods are added that construct frequently used structures. The following code shows how you can use
two facade methods to generate a scatter plot and display it. Note that you need to do this in a class that extends JFXApp.

```scala
import scalafx.application.JFXApp
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer

object PlotTesting extends JFXApp {
  val xPnt = 1 to 10
  val yPnt = xPnt.map(a => a * a)
  val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  FXRenderer(plot)

  val plot2 = Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random), "Random Points", "x", "y")
  FXRenderer(plot2, 1500, 500)
}
```

Currently scatter plots, bar charts, and histograms are the only plotting styles that are implemented. I'm building up [some examples](examples/examples.md) 
that can help you see how to do things.

If you want to use SwiftVis2 in a shell/REPL, including the spark-shell, you can specify the SwiftVis2 JAR file on the command line with the -cp option. Once in the shell you need to import a few things and then run `FXRenderer.startShell()`. This is shown in the following example. You can then repeatedly call `FXRenderer` on various plots as long as you don't close the small window that comes up with `startShell`. 

```scala
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer

FXRenderer.shellStart()
val xPnt = 1 to 10
val yPnt = xPnt.map(a => a * a)
val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
FXRenderer(plot)
```