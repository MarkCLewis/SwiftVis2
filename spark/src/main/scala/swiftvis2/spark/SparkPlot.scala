package swiftvis2.spark

import org.apache.spark.sql.Dataset
import swiftvis2.plotting.Plot
import org.apache.spark.sql.Column

object SparkPlot {
  // TODO - consider a special method for histogram or any other thing that Spark can help with.
  
  /**
   * 
   */
  def histogramPlot(ds: Dataset[_], col: Column, title: String = "", xLabel: String = "", yLabel: String = ""): Plot = {
    ???
  }
}