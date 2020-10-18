package playground

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles._
import swiftvis2.plotting.{Axis, BlackARGB, BlueARGB, Bounds, CategoryAxis, ColorGradient, CyanARGB, Ellipse, GreenARGB, MagentaARGB, NoSymbol, NumericAxis, Plot, Plot2D, PlotDoubleSeries, PlotGrid, PlotIntSeries, PlotLegend, PlotSymbol, PlotText, Rectangle, RedARGB, YellowARGB, WhiteARGB}
import swiftvis2.plotting.styles.ScatterStyle.LineData
import swiftvis2.plotting.Plot.TextData
import swiftvis2.plotting.Plot.GridData

object PlotTesting {
  val xLabel = "x"
  val yLabel = "y"
  val numberFormat = "%1.1f"

  /**
   * Short form, single data examples
   */
  def scatter1(): Plot = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    val cg = (x: Int) => if(x < 5) RedARGB else BlackARGB
    val colors = xPnt.map(cg)
    val sp = Plot.scatterPlot(xPnt, yPnt, title = "Quadratic", xLabel = xLabel, yLabel = yLabel, symbolColor = colors)
    val grid = sp.grids("Main").grid
    new Plot(sp.texts, sp.grids, Seq(PlotLegend.legend(grid, Seq("Red", "Black"))))
  }

  /**
   * Short form, single data with two log axes
   */
  def scatterLogLog(): Plot = {
    val xPnt = 1 to 100
    val yPnt = xPnt.map(a => a * a)
    Plot.scatterPlot(xPnt, yPnt, title = "Quadratic", xLabel = xLabel, xType = Axis.ScaleStyle.LogDense, yLabel = yLabel, yType = Axis.ScaleStyle.LogDense)
  }

  /**
   * Basic scatter plot with a large number of random points.
   */
  def scatter2(): Plot = {
    Plot.scatterPlot((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random),
      title = "Random Points", xLabel = "Independent", yLabel = "Dependent", symbolSize = 2)
  }

  /**
   * Scatter plots with points connected by lines.
   */
  def scatterLines(): Plot = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    Plot.scatterPlotWithLines(xPnt, yPnt, title = "Quadratic", xLabel = xLabel, yLabel = yLabel, lineGrouping = 1)
  }

  /**
   * Scatter plot with error bars on the points.
   */
  def scatterWithErrorBars(): Plot = {
    val xPnt = 1 to 10
    val yPnt = xPnt.map(a => a * a)
    Plot.scatterPlotWithErrorBars(xPnt, yPnt, title = "Quadratic", xLabel = xLabel, yLabel = yLabel, symbolSize = 5, symbolColor = BlackARGB,
      xError = xPnt.map(_ * 0.2), yError = yPnt.map(_ * 0.3))
  }

  /**
   * Short form, multiple data example
   */
  def scatterMultidata(): Plot = {
    Plot.scatterPlots(
      Seq(
        ((1 to 1000).map(_ => math.random * math.random), (1 to 1000).map(_ => math.random * math.random * 0.5), RedARGB, 5),
        ((1 to 1000).map(_ => 1.0 - math.random * math.random), (1 to 1000).map(_ => 1.0 - math.random * math.random * 0.5), GreenARGB, 5)),
      title = "Colored Points", xLabel = "Horizontal", yLabel = "Vertical")
  }

  def scatterGrid(): Plot = {
    val x1 = (1 to 1000).map(_ => math.random)
    val y1 = x1.map(_ * math.random)
    val x2 = 0.0 to 1.1 by 0.01
    val y2 = x2.map(x => math.cos(10 * x * x))
    val x3 = (1 to 1000).map(_ => math.random * math.random)
    val y3 = x2.map(x => math.sin(10 * x * x))
    val c3 = x2.map(_ => math.random)
    val x4 = 0.01 to 1.1 by 0.01
    val y4 = x4.map(x => 0.01 / x)
    val cg = ColorGradient(0.0 -> BlackARGB, 0.5 -> RedARGB, 1.0 -> WhiteARGB)

    Plot.scatterPlotGrid(
      Seq(
        Seq((x1, y1, BlackARGB, 5), (x2, y2, BlueARGB, 5)),
        Seq((x3, y3, cg(c3), 10), (x4, y4, GreenARGB, 5))),
      "Plot Grid", "Shared X", "Shared Y")
  }

  /**
   * Short form, function with color and size
   */
  def scatterWithSizeandColor(): Plot = {
    val xs = 0.0 to 10.0 by 0.01
    val cg = ColorGradient((0.0, RedARGB), (5.0, GreenARGB), (10.0, BlueARGB))
    Plot.scatterPlot(xs, xs.map(math.cos), title = "Cosine", xLabel = "Theta", yLabel = "Value",
        symbolSize = xs.map(x => math.sin(x) + 2), symbolColor = cg(xs))
  }

  def fullScatter(): Plot = {
    val x = 1.0 to 10.0 by 0.1
    val y1 = x.map(_ + math.random - 0.5)
    val y2 = x
    val color = BlackARGB: PlotIntSeries
    val size1 = 5: PlotDoubleSeries
    val size2 = 0: PlotDoubleSeries
    val stroke = Renderer.StrokeData(1, Nil)
    val xerr = x.map(_ => 0.2 * math.random)
    val yerr = x.map(_ => 0.2 * math.random)
    Plot.scatterPlotsFull(Seq((x, y1, color, size1, None, Some(xerr), Some(yerr)),
        (x, y2, color, size2, Some(ScatterStyle.LineData(0, stroke)), None, None)))
  }

  /**
   * Short form bar plot
   */
  def barChart(): Plot = {
    import BarStyle._
    Plot.barPlot(Seq("red", "green", "blue"), Seq(DataAndColor(Seq(3.0, 7.0, 4.0), YellowARGB), DataAndColor(Seq(2.0, 1.0, 3.0), MagentaARGB)),
        true, 0.8, "Bar Plot", "Colors", "Measure")
  }

  /**
   * Short form histogram plot
   */
  def histogram(): Plot = {
    val bins = 0.0 to 10.0 by 1.0
    Plot.histogramPlot(bins, bins.map(12 - _).init, BlueARGB, false, "Histogram Plot", "Value", "Count")
  }

  def histogramFromData(): Plot = {
    val bins = -1.0 to 1.0 by 0.1
    val data = (1 to 10000).map(_ => math.cos(math.random*math.Pi))
    Plot.histogramPlotFromData(bins, data, GreenARGB)
  }

  /**
   * Short form histogram plot
   */
  def histogramSide(): Plot = {
    val bins = 0.0 to 10.0 by 1.0
    Plot.histogramPlot(bins, bins.map(12 - _).init, BlueARGB, false, "Histogram Plot", "Value", "Count", false)
  }

  /**
   * Short form histogram plot
   */
  def histogram2(): Plot = {
    val bins = 1.0 to 10.1 by 1.0
    import HistogramStyle.DataAndColor
    Plot.stackedHistogramPlot(bins, Seq(DataAndColor(bins.map(12 - _), BlueARGB), DataAndColor(bins.map(x => 5 * (math.cos(x) + 2)), 0xffff0000)),
        true, "Histogram Plot", "Value", "Count")
  }

  /**
   * Short form grid of histogram plots
   */
  def histogramGrid(): Plot = {
    val bins = 0.0 to 10.0 by 1.0
    import HistogramStyle.DataAndColor
    Plot.histogramGrid(bins, Seq(
      Seq(DataAndColor(bins.map(12 - _).init, RedARGB), DataAndColor(bins.map(1 + _).init, GreenARGB)),
      Seq(DataAndColor(bins.map(c => c * c / 6).tail, BlueARGB), DataAndColor(bins.map(c => 10 * math.random).init, CyanARGB))),
      false, false, "Histogram Grid", "Values", "Counts")
  }

  /**
   * Long form - this example shows the general capabilities of the plot grid and adding multiple plots
   */
  def longForm(): Plot = {
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
  }

  def saveToFile(): Unit = {
    val plot = Plot.scatterPlots(
      Seq(
        ((1 to 10000).map(_ => math.random * math.random), (1 to 10000).map(_ => math.random * math.random * 0.5), RedARGB, 5),
        ((1 to 10000).map(_ => 1.0 - math.random * math.random), (1 to 10000).map(_ => 1.0 - math.random * math.random * 0.5), GreenARGB, 5)),
      title = "Colored Points", xLabel = "Horizontal", yLabel = "Vertical")
    //FXRenderer.saveToImage(plot, 1200, 700, new File("sample.png"))
  }

  def colorTest(): Plot = {
    val cg = ColorGradient(0.0 -> BlueARGB, 1.0 -> RedARGB, 2.0 -> GreenARGB)
    Plot.scatterPlot(Seq(-1, 0, 1), Seq(-1, 0, 1), title = "Title", xLabel = xLabel, yLabel = yLabel, symbolSize = 10,
        symbolColor = cg(Array(0.0, 1.0, 2.0)))
  }

  def boxPlot(): Plot = {
    val categories = Array("Random 1", "Random 2", "Random 3")
    val data = categories.map(_ => Array.fill(1000)((math.random + 0.5) * (math.random + 0.5)): PlotDoubleSeries)
    Plot.boxPlot(categories, data, title = "Box Plot", yLabel = "Random values")
  }

  def violinPlot(): Plot = {
    val categories = Array("Random 1", "Random 2", "Random 3")
    val data = categories.map(_ => Array.fill(1000)((math.random + 0.5) * (math.random + 0.5)): PlotDoubleSeries)
    Plot.violinPlot(categories, data, title = "Violin Plot", yLabel = "Random values")
  }

  def stackedNNTest(): Plot = {
    val xs = (1 to 1000).map(_ => math.random * math.random)
    val ys = (1 to 1000).map(_ => math.random * math.random * 50)
    val cntsMap = xs.groupBy(x => (x * 10).toInt)
    val cnts = Array.tabulate(10)(i => cntsMap.get(i).getOrElse(Nil).length): PlotDoubleSeries
    Plot.stackedNN(
      Seq(HistogramStyle(0.0 to 1.0 by 0.1, Array(HistogramStyle.DataAndColor(cnts, RedARGB))), ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5)),
      title = "Stacked NN", xLabel = "X", yLabel = "Y")
  }

  def gridNNTest(): Plot = {
    val xs = (1 to 1000).map(_ => math.random * math.random)
    val ys = (1 to 1000).map(_ => math.random * math.random * 50)
    val cntsMap = xs.groupBy(x => (x * 10).toInt)
    val cnts = Array.tabulate(10)(i => cntsMap.get(i).getOrElse(Nil).length): PlotDoubleSeries
    Plot.gridNN(
      Seq(Seq(ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5)), Seq(HistogramStyle(0.0 to 1.0 by 0.1, Array(HistogramStyle.DataAndColor(cnts, RedARGB))))),
      title = "Grid NN", xLabel = "X", yLabel = "Y")
  }

  def stackedCNTest(): Plot = {
    val cats = Array("One", "Two", "Three")
    val ys = Array[PlotDoubleSeries](
      (1 to 1000).map(_ => (math.random - 0.5) * (math.random - 0.5) * 2 + 1),
      (1 to 1000).map(_ => (math.random - 0.5) * (math.random - 0.5) * 4 + 2),
      (1 to 1000).map(_ => (math.random - 0.5) * (math.random - 0.5) * 3 + 1.5))
    Plot.stackedCN(
      Seq(BarStyle(cats, Array(BarStyle.DataAndColor(Array(1, 2, 3), YellowARGB))), BoxPlotStyle(cats, ys), ViolinPlotStyle(cats, ys)),
      title = "Stacked CN", xLabel = "Categories", yLabel = "Y")
  }

  def gridCNTest(): Plot = {
    val cats = Array("One", "Two", "Three")
    val ys = Array[PlotDoubleSeries](
      (1 to 1000).map(_ => (math.random - 0.5) * (math.random - 0.5) * 2 + 1),
      (1 to 1000).map(_ => (math.random - 0.5) * (math.random - 0.5) * 4 + 2),
      (1 to 1000).map(_ => (math.random - 0.5) * (math.random - 0.5) * 3 + 1.5))
    Plot.gridCN(
      Seq(Seq(BarStyle(cats, Array(BarStyle.DataAndColor(Array(1, 2, 3), YellowARGB))), BoxPlotStyle(cats, ys), ViolinPlotStyle(cats, ys))),
      title = "Grid CN", xLabel = "Categories", yLabel = "Y")
  }

  def rowOfDists(): Plot = {
    val xs = Array.fill(900)(math.random * 100)
    val ys = xs.map(_ => (math.random - 0.5) * (math.random - 0.5) * 4 + 1)
    val bins = 0.0 to 2.0 by 0.05
    val cnts = Array.fill(bins.length - 1)(0.0)
    for (y <- ys) {
      val bin = (y / 0.05).toInt
      if (bin >= 0 && bin < cnts.length) cnts(bin) += 1
    }
    Plot.row(Seq(
      ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5),
      HistogramStyle(bins, Seq(HistogramStyle.DataAndColor(cnts, GreenARGB)), binsOnX = false),
      BoxPlotStyle(Array("Distrib"), Array(ys)),
      ViolinPlotStyle(Array("Distrib"), Array(ys))), "Distributions", "Num X", "Categories", "Y")
  }

  def colOfDists(): Plot = {
    val xs = Array.fill(900)(math.random * 100)
    val ys = xs.map(_ => (math.random - 0.5) * (math.random - 0.5) * 4 + 1)
    val bins = 0.0 to 2.0 by 0.05
    val cnts = Array.fill(bins.length - 1)(0.0)
    for (y <- ys) {
      val bin = (y / 0.05).toInt
      if (bin >= 0 && bin < cnts.length) cnts(bin) += 1
    }
    Plot.row(Seq(
      ScatterStyle(xs, ys, symbolWidth = 5, symbolHeight = 5),
      HistogramStyle(bins, Seq(HistogramStyle.DataAndColor(cnts, GreenARGB)), binsOnX = false)))
  }

  def pressureTempPlot(): Plot = {
    val alt = Array(-0.6, 11, 20, 32, 47, 51, 71, 84.852)
    val temp = Array(19.0, -56.5, -56.5, -44.5, -2.5, -2.5, -58.5, -86.38)
    val pressure = Array(108900, 22632, 5474.9, 868.02, 110.91, 66.939, 3.9564, 0.3734)
    Plot.stacked(Seq(
        ScatterStyle(temp, alt, lines=ScatterStyle.connectAll),
        ScatterStyle(pressure, alt, lines=ScatterStyle.connectAll, symbol = Rectangle)),
        "Temp and Pressure", "Temperature [C]", "Altitude [km]").
        withModifiedAxis[NumericAxis](xLabel, "pressure",
            _.asMaxSideXAxis
             .updatedScaleStyle(Axis.ScaleStyle.LogSparse)
             .updatedName("Pressure [Pa]")).
        updatedStyleXAxis("pressure", stack = 1)
  }

  def coloredSurfacePlot(): Plot = {
    val n = 20
    val xs = (0 to n).map(_ / n.toDouble)
    val ys = (0 to n).map(_ / n.toDouble)
    val allX = for(x <- xs; y <- ys) yield x
    val allY = for(x <- xs; y <- ys) yield y
    val allC = for(x <- xs; y <- ys) yield (0xff << 24) | ((x*255).toInt << 16) | (y*255).toInt
    Plot.simple(ColoredSurfaceStyle(allX, allY, allX, allC), "Surface Plot", "X", "Y")
  }

  def ringPlotGridBug(): Plot = {
    val numParts = 10000
    val plotStyles = (1 to 3).map { p =>
      val cg = ColorGradient(0.0 -> BlueARGB, 90.0 -> GreenARGB)
      val radii = (1 to numParts).map(_ => 0.01 * math.random + 0.005)
      (ScatterStyle((0 until numParts).map(_ => math.random), (0 until numParts).map(_ => p + math.random), symbolWidth = radii, symbolHeight = radii, xSizing = PlotSymbol.Sizing.Scaled,
          ySizing = PlotSymbol.Sizing.Scaled, colors = BlackARGB),
        (5 to 75 by 15).map { case b =>
            ScatterStyle(0.0 to 1.0 by 0.05, (1 to 20).map(i => math.random * 100),
              symbol = NoSymbol, 
              lines = Some(LineData(0)),
              colors = cg(b))
        }
      )
    }
    val font = Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val photonCountAxis = NumericAxis(None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings("Photon Count", font)), Axis.DisplaySide.Max)
    val yAxes = plotStyles.zipWithIndex.map { case (_, i) => NumericAxis(None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(0.0, font, "%1.1f")), Some(Axis.NameSettings("Azimuthal Position", font)), Axis.DisplaySide.Min) }
    val xAxis = NumericAxis(None, None, None, Axis.TickStyle.Both,
      Some(Axis.LabelSettings(90.0, font, "%1.1f")), Some(Axis.NameSettings("Radial Position", font)), Axis.DisplaySide.Min)
    val grid = PlotGrid(plotStyles.zipWithIndex.map { case ((cart, scans), i) =>
      Seq(Seq(Plot2D(cart, "X", "Y"+i)), scans.map(scan => Plot2D(scan, "X", "Count"))) },
      Map("X" -> xAxis, "Count" -> photonCountAxis) ++ yAxes.zipWithIndex.map { case (a, i) => ("Y"+i) -> a },
      Seq(1.0, 1.0),
      plotStyles.map(_ => 1.0)
    )
    // println(grid)
    Plot(Map("Title" -> TextData(PlotText("Funky Plot Test"), Bounds(0, 0, 1.0, 0.05))),
      Map("Main" -> GridData(grid, Bounds(0.01, 0.05, 0.99, 0.95))))
  }

  def simpleFull(): Plot = {
    val x = 1.0 to 10.0 by 0.5
    val y = x.map(a => a * a)
    val font = new Renderer.FontData("Ariel", Renderer.FontStyle.Plain)
    val style = ScatterStyle(x, y)
    val p2d = Plot2D(style, "x", "y")
    val xAxis = NumericAxis(tickLabelInfo = Some(Axis.LabelSettings(90, font, numberFormat)), name = Some(Axis.NameSettings("X", font)))
    val yAxis = NumericAxis(tickLabelInfo = Some(Axis.LabelSettings(0, font, numberFormat)), name = Some(Axis.NameSettings("Y", font)))
    val grid = PlotGrid(Seq(Seq(Seq(p2d))), Map("x" -> xAxis, "y" -> yAxis), Seq(1.0), Seq(1.0))
    Plot(grids = Map("main" -> Plot.GridData(grid, Bounds(0.0, 0.05, 0.95, 0.95))))
  }

  def performanceTest(): Plot = {
    val x = Array.fill(1000000)((math.random-0.5)*(math.random-0.5))
    val y = x.map(_ => math.cos(math.random*math.random*6.28))
    Plot.scatterPlot(x, y, "Big", xLabel, yLabel, 0.001, BlackARGB, xSizing = PlotSymbol.Sizing.Scaled, ySizing = PlotSymbol.Sizing.Scaled)
  }

  def gridSkipAxisLabels(): Plot = {
    val x = (0 to 10).map(_.toDouble) :+ 10.3
    val y = (0 to 10).map(x => x*x) :+ 101
    val scatter = ScatterStyle(x, y)
    val scatter2 = ScatterStyle(x, x)
    Plot.gridNN(Seq(Seq(scatter, scatter), Seq(scatter, scatter2)))
  }

  def buildUpFluent(): Plot = {
    val xs = (0 to 10).map(_.toDouble)
    val ys = xs.map(x => math.sqrt(x))
    val counts = (0 to 9).map(i => 1.0 - i*i/100.0)
    Plot.simple(ScatterStyle(xs, xs))
      .withAxis("y2", NumericAxis.defaultVerticalAxis("Y2").min(0.0).maxSide)
      .updatedAxis[NumericAxis]("x", _.updatedName("X"))
      .updatedAxis[NumericAxis]("x", _.updatedName("Y"))
      .updatedPlotGrid(_.withRow().withColumn().withRow(0).withColumn(0)
        .withStyle(ScatterStyle(xs, ys), "x", "y", 0, 0, 0)
        .withStyle(HistogramStyle(xs, Seq(HistogramStyle.DataAndColor(counts, RedARGB))), "x", "y2", 0, 1, 0)
        .withStyle(ScatterStyle(xs, ys), "x", "y", 0, 2, 0)
        .withStyle(HistogramStyle(xs, Seq(HistogramStyle.DataAndColor(counts, RedARGB))), "x", "y2", 1, 0, 0)
        .withStyle(ScatterStyle(xs, counts), "x", "y", 1, 1, 1)
        .withStyle(HistogramStyle(xs, Seq(HistogramStyle.DataAndColor(counts, RedARGB))), "x", "y2", 1, 2, 0)
        .withStyle(ScatterStyle(xs, ys), "x", "y", 2, 0, 0)
        .withStyle(HistogramStyle(xs, Seq(HistogramStyle.DataAndColor(counts, RedARGB))), "x", "y2", 2, 1, 0)
        .withStyle(ScatterStyle(xs, ys), "x", "y", 2, 2, 0)
      )
  }

  val largeDim = 1000
  val medDim = 800
  val smallDim = 600
  val pubDim = 400
}
