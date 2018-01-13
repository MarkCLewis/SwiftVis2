package playground

import org.apache.spark.sql.SparkSession
import scalafx.application.JFXApp
import swiftvis2.spark._
import swiftvis2.plotting._
import swiftvis2.plotting.renderer.FXRenderer
import swiftvis2.plotting.renderer.Renderer
import org.apache.spark.sql.Column

case class Point(x: Double, y: Double, size: Double = 5, color: Int = BlackARGB, line: Int = 0, xerr: Double = 0, yerr: Double = 0)

/**
 * Test code for spark based plots.
 */
object SparkPlotTesting extends JFXApp {
  implicit val spark = SparkSession.builder.appName("NOAA Cluster Data").master("local[*]").getOrCreate()
  import spark.implicits._

  spark.sparkContext.setLogLevel("WARN")

  val pnts = Array.tabulate(100)(i => Point(i, i * i, 3 + 3 * math.random, ARGB(1, math.random, math.random, math.random), util.Random.nextInt(5), math.random * 10, math.random * 500))

  {
    implicit val df = spark.createDataset(pnts)

    val plot1 = Plot.scatterPlot('x, 'y, "Spark Based Plot", "x", "y")
    FXRenderer(plot1, 600, 600)

    val plot2 = Plot.scatterPlot('x, 'y, "Spark Based Plot", "x", "y", 'size, 'color)
    FXRenderer(plot2, 600, 600)

    pnts foreach println
    val stroke = Renderer.StrokeData(1, Nil)
    val plot3 = Plot.scatterPlotsFull(Array(('x: PlotDoubleSeries, 'y: PlotDoubleSeries, 'color: PlotIntSeries, 'size: PlotDoubleSeries, Some(('line: PlotDoubleSeries) -> stroke), Some('xerr: PlotDoubleSeries), Some('yerr: PlotDoubleSeries))), "Spark Based Plot", "x", "y")
    FXRenderer(plot3, 600, 600)
  }

  {
    val df = spark.createDataset(pnts)
    
    val plot1b = Plot.scatterPlot(doubles(df,'x), doubles(df,'y), "Spark Based Plot", "x", "y")
    FXRenderer(plot1b, 600, 600)

    val plot2b = Plot.scatterPlot(doubles(df,'x), doubles(df,'y), "Spark Based Plot", "x", "y", doubles(df,'size), ints(df,'color))
    FXRenderer(plot2b, 600, 600)

    pnts foreach println
    val stroke = Renderer.StrokeData(1, Nil)
    val plot3b = Plot.scatterPlotsFull(Array((doubles(df,'x), doubles(df,'y), ints(df,'color), doubles(df,'size), Some(doubles(df,'line) -> stroke), Some(doubles(df,'xerr)), Some(doubles(df,'yerr)))), "Spark Based Plot", "x", "y")
    FXRenderer(plot3b, 600, 600)
  }

  spark.stop()
}