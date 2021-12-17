# SwiftVis

This project is an updated version of SwiftVis written in Scala with better support for parallelism, support for rendering with diverse platforms,
and a programming interface. I try to keep a reasonably up to date API posted [on my personal site](https://www.cs.trinity.edu/~mlewis/SwiftVis2/).

SwiftVis2 is still in the early stages. I am focusing on adding basic plotting capability with the programming interface currently.
The graphical dataflow interface and other features will follow. Currently I also have a research student, Nick Smoker, working on making
the code cross-compile for Scala.js and Scala Native so that plotting works on all available platforms.

## Updates

- 6/21/2020: Pushed an update to use JavaFX for Java 11. If you had built previously, you might need to manually delete fxrenderer/target
for this to compile and run nicely. Doing a 'clean' in sbt doesn't remove some files that need to be updated. Unfortunately, this does break
the Spark tests, even after updating to Spark 3.0.0, which is supposed to work with Java 11. We'll have to look into this more, but the spark
extentions probably aren't safe to use right now. More testing is needed with manual pulling of the data.

## Installation

SwiftVis2 is not yet at the point where it belongs in a Maven repository. Until SwiftVis2 is stable enough to put in a Maven repository 
you can use it in one of two ways.

1. Run `publishLocal` in sbt and include the appropriate dependencies in your `build.sbt` file. Note that you probably don't need both the JavaFX and the Swing libraries. Pick whichever you are using.
  * `libraryDependencies += "edu.trinity" %% "swiftvis2core" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2jvm" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2fx" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2swing" % "0.1.0-SNAPSHOT"`
2. Compile and package this project and put the JAR files in the `lib` directory of your sbt project.

If you want to use SwiftVis2 with Spark, you should probably use the `publishLocal` option, but with some modifications.

1. Spark now supports Scala 2.12, but many versions don't do so by default. Check your version. If your Spark is using Scala 2.11 then run `++2.11.12` to set the Scala version to 2.11. You can update the last value to whatever the latest release is.
2. Run `publishLocal` to publish the 2.11 version of the main SwiftVis2 library.
3. Run `spark/publishLocal` to publish the Spark integration library.
4. Add the following lines to your build.sbt. Again, you probably won't use both JavaFX and Swing so leave out the one you don't need.
  * `libraryDependencies += "edu.trinity" %% "swiftvis2core" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2jvm" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2fx" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2swing" % "0.1.0-SNAPSHOT"`
  * `libraryDependencies += "edu.trinity" %% "swiftvis2spark" % "0.1.0-SNAPSHOT"`
  
Note that the `sbt` commands can all be done at onced from the command line with `sbt ++2.11.12 publishLocal spark/publishLocal`.

## Usage 

While it is possible to build plots piece by piece, the fact that SwiftVis2 supports a lot of plotting options can make that tedious.
To help with that, facade methods are added that construct frequently used structures. The following code shows how you can use
two facade methods to generate a scatter plot and display it. Note that you need to do this in a class that extends JFXApp3
to use the FXRenderer. That isn't needed for other renderers. Based on our expereince, at this point we would recommend using 
the SwingRenderer over the FXRenderer. It is easier to use, faster, and produces slightly nicer output.

A sample application using JavaFX might look like the following.

```scala
import scalafx.application.JFXApp3
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer

object PlotTesting extends JFXApp3 {
  val xPnt = 1 to 10
  val yPnt = xPnt.map(a => a * a)
  val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  FXRenderer(plot)

  val plot2 = Plot.scatterPlot((1 to 1000).map(_ => math.random() * math.random()), (1 to 1000).map(_ => math.random() * math.random()), "Random Points", "x", "y")
  FXRenderer(plot2, 1500, 500)
}
```

This same example using Swing would look like the following.

```scala
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.SwingRenderer

object PlotTesting extends App {
  val xPnt = 1 to 10
  val yPnt = xPnt.map(a => a * a)
  val plot = Plot.scatterPlot(xPnt, yPnt, "Quadratic", "x", "y")
  SwingRenderer(plot)

  val plot2 = Plot.scatterPlot((1 to 1000).map(_ => math.random() * math.random()), (1 to 1000).map(_ => math.random() * math.random()), "Random Points", "x", "y")
  SwingRenderer(plot2, 1500, 500, true)  // The true at the end means that closing this window terminates the application.
}
```

Currently scatter plots, bar charts, histograms, box and whisker plots, and violin plots are the only plotting styles that are implemented. I'm building up 
[some examples](examples/examples.md) that can help you see how to do things.

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

### Polynote Integration

SwiftVis2 integrates with [the Polynote Scala notebook](https://polynote.org/). To use SwiftVis2 with Polynote, you will first need to publishLocal the dependencies with `coreJVM/publishLocal`, `swing/publishLocal` and `polynote/publishLocal`. After this, add the following lines to the scala dependencies section of the `config.yml` file in your polynote directory: 
  * `- edu.trinity:swiftvis2polynote_2.12:0.1.0-SNAPSHOT`
  * `- edu.trinity:edu.trinity:swiftvis2core_2.12:0.1.0-SNAPSHOT`
  * `- edu.trinity:swiftvis2swing_2.12:0.1.0-SNAPSHOT`

More information on the formatting of the config.yml file can be found in the `config-template.yml` file included with polynote.

After this, you should make sure the integration is running by inputting the following code into a notebook:
```scala
import swiftvis2.plotting.Plot

val data = 1 to 10

Plot.scatterPlot(data, data, title = "Hooray, it works!")
```

If a plot pops up, you're good to use SwiftVis2 with Polynote, the same as you would in any other setting (minus the rendering calls). Note that Polynote does not update dependencies for existing notebooks, so if you want to use SwiftVis2 with a notebook you've already made, you'll need to add the dependencies to that notebook manually.
