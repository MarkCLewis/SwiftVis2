# SwiftVis

This project is an updated version of SwiftVis written in Scala with better support for parallelism, support for rendering with diverse platforms,
and a programming interface. I try to keep a reasonably up to date API posted [on my personal site](http://www.cs.trinity.edu/~mlewis/SwiftVis2/api/).

SwiftVis2 is still in the early stages. I am focusing on adding basic plotting capability with the programming interface currently.
The graphical dataflow interface and other features will follow. Currently I also have a research student, Nick Smoker, working on making
the code cross-compile for Scala.js and Scala Native so that plotting works on all available platforms.

## Installation

SwiftVis2 is not yet at the point where it belongs in a Maven repository. Until SwiftVis2 is stable enough to put in a Maven repository 
you can use it in one of two ways.

1. Run `publishLocal` in sbt and include the appropriate dependency in your `build.sbt` file.
  * `libraryDependencies += "edu.trinity" %% "swiftvis2" % "0.1.0-SNAPSHOT"`
2. Compile and package this project and put the JAR file in the `lib` directory of your sbt project.

If you want to use SwiftVis2 with Spark, you should probably use the `publishLocal` option, but with some modifications.

1. Run `++2.11.12` to set the Scala version to 2.11. You can update the last value to whatever the latest release is. This is required because Spark currently doesn't work with Scala 2.12 or newer.
2. Run `publishLocal` to publish the 2.11 version of the main SwiftVis2 library.
3. Run `spark/publishLocal` to publish the Spark integration library.
4. Add the following lines to your build.sbt
  * `libraryDependencies += "edu.trinity" %% "swiftvis2" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2spark" % "0.1.0-SNAPSHOT"`
  
Note that the `sbt` commands can all be done at onced from the command line with `sbt ++2.11.12 publishLocal spark/publishLocal`.

## Usage 

While it is possible to build plots piece by piece, the fact that SwiftVis2 supports a lot of plotting options can make that tedious.
To help with that, facade methods are added that construct frequently used structures. The following code shows how you can use
two facade methods to generate a scatter plot and display it. Note that you need to do this in a class that extends JFXApp
to use the FXRenderer. That isn't needed for other renderers. Based on our expereince, at this point we would recommend using 
the SwingRenderer over the FXRenderer. It is easier to use, faster, and produces slightly nicer output.

```scala
import scalafx.application.JFXApp
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.fxrenderer.FXRenderer

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

If you want to use SwiftVis2 in a shell/REPL, including the spark-shell, you can specify the SwiftVis2 JAR file on the command line with the -cp option. Once in the shell you need to import a few things probably including `swiftvis2.plotting._` and `swiftvis2.plotting.renderer._`.  If you are using the FXRenderer you then run `FXRenderer.startShell()`. This is shown in the following example. You can then repeatedly call `FXRenderer` on various plots as long as you don't close the small window that comes up with `startShell`. If you are using the SwingRenderer, you can skip that step and just display plots. There won't be extra windows, but you need to be careful not to have your windows exit on close.

```scala
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.fxrenderer.FXRenderer

FXRenderer.shellStart()
val xPnt = 1 to 10
val yPnt = xPnt.map(a => a * a)
val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
FXRenderer(plot)
```
