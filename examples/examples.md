# SwiftVis 2 Examples

This is where you can find various examples of SwiftVis2 usage. Until this is finished, you'll find code to produce the testings plots
in [playground.PlotTesting](https://github.com/MarkCLewis/SwiftVis2/blob/master/src/main/scala/playground/PlotTesting.scala). Code that renders the plots can also be found for 
the [FXRenderer](https://github.com/MarkCLewis/SwiftVis2/blob/master/src/main/scala/playground/JFXPlotTesting.scala), 
the [SwingRenderer](https://github.com/MarkCLewis/SwiftVis2/blob/master/src/main/scala/playground/SwingPlotTesting.scala), 
and the [SVGRenderer](https://github.com/MarkCLewis/SwiftVis2/blob/master/src/main/scala/playground/SVGPlotTesting.scala).
For normal display purposes, we recommend that you use the SwingRenderer both for simplicity and speed. The only advantage of the FXRenderer is that it displays incrementally so you will see stuff being drawn before it is all finished.

## Programmatic Usage

I'm working first to develop the ability to do programmatic plotting. A big part of the goal here is to make something that people can use to quickly
and easily plot data with Spark when using Scala. For that reason, I have defined a number of facade methods in the `Plot` object that provide a
simple interface for producing plots.

### Original Facade

The original facade for SwiftVis included a number of methods for producing specific types of plots. These are listed first here because they show
you the different styles of plots supported by SwiftVis2, but for a lot of purposes you will probably find that the new facade, described below,
is superior as it gives you a bit more control and allows you to mix different types of plot styles in a single plot.

#### Scatter Plots

This is almost certainly the most common plot style in scientific plotting. As such, it is well developed in SwiftVis2. In addition to the standard
abilities to provide x and y values for points, the SwiftVis2 scatter plot allows you to provide per point colors, point sizes, and error bars.
The points can also be connected with lines using a mechanism that allows you to make multiple line plots from a single set of data.

Here is a simple example of two data sets plotted in different colors that share the same axes. Note that all code samples assume data is defined in arrays or sequences of `Double`s.

```scala
Plot.scatterPlots(
  Seq((x1, y1, RedARGB, 5), (x2, y2, GreenARGB, 5)),
  title = "Colored Points", xLabel = "Horizontal", yLabel = "Vertical")
```

![colored scatter](colordots.png "Colored Scatter Plot")

#### Scatter Plot Grid

SwiftVis2 plots are really just combinations of a few basic elements including text and plot grids. Most of the time people use 1x1 grids to have all
plots in a single area as the example above demonstrates. There is a simple facade method that makes regular grids of scatter plots like the example
below. 

```scala
Plot.scatterPlotGrid(
  Seq(
    Seq((x1, y1, BlackARGB, 5), (x2, y2, BlueARGB, 5)),
    Seq((x3, y3, cg(c3), 10), (x4, y4, GreenARGB, 5))),
  "Plot Grid", "Shared X", "Shared Y")
```

![scatter grid](plotGrid.png "Grid of Scatter Plots")

#### Histogram

The histogram style can be drawn with values stacked or not. It can also draw the bins either centered on the bin value or spanning the bin values.
Note that the histogram method in Spark produces the latter option. This is significant as when you don't center, the bin data needs to have one more
element than the counts.

```scala
Plot.stackedHistogramPlot(bins, Seq(
		DataAndColor(counts1, BlueARGB), 
		DataAndColor(counts2, RedARGB)), 
  true, "Histogram Plot", "Value", "Count")
```

![histogram](histogram.png "Simple Histogram")

Note that the above example expects you provide counts. This is because tools
like Spark provide methods that do this for you in an efficient, distributed
way. If your data set fits nicely on a single machine the following code will
do the binning and make a histogram from the data for you.

```scala
Plot.histogramPlotFromData(bins, data, GreenARGB)
```
![histogram from data](histogramFromData.png "Histogram from Data")

#### Bar Charts

The bar chart utilizes a categorical axis. The bars can be drawn side-by-side or stacked.

```scala
Plot.barPlot(categories, Seq(
		DataAndColor(values1, YellowARGB), 
		DataAndColor(values2, MagentaARGB)), 
  true, 0.8, "Bar Plot", "Colors", "Measure")
```

![bar chart](bar.png "Simple Bar Chart")

#### Box Plots

The box plot has a categorical X-axis and a numeric Y-axis. The user can provide whatever values desired for the data on the boxes, but a convenience
method in the companion object calculates the quartiles and puts the min and max values no more than 1.5 IRQs above or below the quartiles, then draws
outliers beyond those values. 

```scala
Plot.boxPlot(categories, data, title = "Box Plot", yLabel = "Random values")
```

![box plot](boxPlot.png "Simple Box Plot")

#### Violin Plots

The violin plot has a categorical X-axis and a numeric Y-axis. The user provides the data and an option bandwith. The density function is approximated
using normal kernels and lines are drawn for the quartiles.

```scala
Plot.violinPlot(categories, data, title = "Violin Plot", yLabel = "Random values")
```

![violin plot](violinPlot.png "Simple Violin Plot")

### Updated Facade

The updated facade includes methods that allow you to put different plot
styles in a single plot. They let you pass in the various plot styles and there
are different options for the axis tpes that are used.

#### Simple

#### Stacked

#### Row

```scala
Plot.row(Seq(
  ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5),
  HistogramStyle(bins, Seq(HistogramStyle.DataAndColor(cnts, GreenARGB)), binsOnX = false),
  BoxPlotStyle(Array("Distrib"), Array(ys)),
  ViolinPlotStyle(Array("Distrib"), Array(ys))), "Distributions", "Num X", "Categories", "Y")
```

![row plot](row.png "Row Plot")

#### Column - TODO

#### Stacked Numeric-Numeric

```scala
Plot.stackedNN(
  Seq(HistogramStyle(0.0 to 1.0 by 0.1, Array(HistogramStyle.DataAndColor(cnts, RedARGB))), ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5)),
  title = "Stacked NN", xLabel = "X", yLabel = "Y")
```

![stackedNN plot](stackedNN.png "Stacked Numeric-Numeric Plot")

#### Grid Numeric-Numeric

```scala
Plot.gridNN(
  Seq(Seq(ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5)), Seq(HistogramStyle(0.0 to 1.0 by 0.1, Array(HistogramStyle.DataAndColor(cnts, RedARGB))))),
  title = "Grid NN", xLabel = "X", yLabel = "Y")
```
![gridNN plot](gridNN.png "Grid Numeric-Numeric Plot")

#### Stacked Category-Numeric

```scala
Plot.stackedCN(
  Seq(BarStyle(cats, Array(BarStyle.DataAndColor(Array(1, 2, 3), YellowARGB))), BoxPlotStyle(cats, ys), ViolinPlotStyle(cats, ys)),
  title = "Stacked CN", xLabel = "Categories", yLabel = "Y")
```
![stackedCN plot](stackedCN.png "Stacked Category-Numeric Plot")

#### Grid Category-Numeric

```scala
Plot.gridCN(
  Seq(Seq(BarStyle(cats, Array(BarStyle.DataAndColor(Array(1, 2, 3), YellowARGB))), BoxPlotStyle(cats, ys), ViolinPlotStyle(cats, ys))),
  title = "Grid CN", xLabel = "Categories", yLabel = "Y")
```
![gridCN plot](gridCN.png "Grid Category-Numeric Plot")


### Fluent Interface

The methods in the facade can't handle all the possiblities that some might 
want for their plots. One way to deal with this is to use the facade to build 
a plot that is close to what you want, then use the fluent interface to alter 
some element of it to match what you really want.

```scala
Plot.stacked(Seq(
  ScatterStyle(temp, alt, lines=ScatterStyle.connectAll), 
  ScatterStyle(pressure, alt, lines=ScatterStyle.connectAll, symbol = Rectangle)), 
  "Temp and Pressure", "Temperature [C]", "Altitude [km]").
  withModifiedAxis[NumericAxis](xLabel, "pressure", 
      _.asMaxSideXAxis
       .updatedScaleStyle(Axis.ScaleStyle.LogSparse)
       .updatedName("Pressure [Pa]")).
  updatedStyleXAxis("pressure", stack = 1)
```
![stacked fluent plot](stackedFluent.png "Stacked Fluent Plot")

### Long-form/Non-facade

All the plots above were made using facade methods that provide a shortcut syntax. However, you can also construct your own plot grids with whatever
plot styles and axes you want, with variable width rows and columns. An example of this, which also shows many of the different capabilities of SwiftVis2
is shown here.

```scala
val font = new Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
val xAxis1 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(90, font, numberFormat)), 
    Some(Axis.NameSettings("X1", font)))
val xAxis2 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(90, font, numberFormat)), 
    Some(Axis.NameSettings("X2", font)))
val xAxisCat = new CategoryAxis(Axis.TickStyle.Both, 0, font, Some(Axis.NameSettings("Categories", font)), Axis.DisplaySide.Max)
val yAxis1 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, numberFormat)), 
    Some(Axis.NameSettings("Y1", font)))
val yAxis2 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.0f")), 
    Some(Axis.NameSettings("Y2", font)))
val yAxis3 = new NumericAxis(None, None, None, Axis.TickStyle.Both, Some(Axis.LabelSettings(0, font, "%1.0f")), 
    Some(Axis.NameSettings("Y3", font)), Axis.DisplaySide.Max)

// Main Scatter plot
val (mainX, mainY) = (for (_ <- 1 to 1000) yield {
  val r = math.random * math.random * math.random
  val theta = math.random * 2 * math.Pi
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
val x2 = Array.fill(100)(math.random)
val y2 = x2.map(x => math.cos(x * 3) + 0.2 * math.random)
val ex2 = x2.map(x => 0.1 * math.random)
val ey2 = x2.map(x => 0.2 * math.random)
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
```

![complex plot](complexPlot.png "Complex Plot")

## Graphical Interface Usage

The graphical interface isn't currently implemented.
