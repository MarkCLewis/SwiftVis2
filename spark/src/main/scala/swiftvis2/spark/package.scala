package swiftvis2

import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer

/**
 * This object has convenience methods specifically for working with Spark. If you are plotting from a Spark application, you should
 * import the contents of this object.
 */
package object spark {
  implicit class ColumnToIntSeries(col: Column)(implicit ds: Dataset[_]) extends PlotIntSeries {
    import ds.sparkSession.implicits._
    val data = ds.select(col).as[Int].collect()
    def minIndex: Int = 0
    def maxIndex: Int = data.length
    def apply(i: Int) = data(i)
  }
  implicit class ColumnToDoubleSeries(col: Column)(implicit ds: Dataset[_]) extends PlotDoubleSeries {
    import ds.sparkSession.implicits._
    val data = ds.select(col).as[Double].collect()
    def minIndex: Int = 0
    def maxIndex: Int = data.length
    def apply(i: Int) = data(i)
  }
  implicit class ColumnToStringSeries(col: Column)(implicit ds: Dataset[_]) extends PlotStringSeries {
    import ds.sparkSession.implicits._
    val data = ds.select(col).as[String].collect()
    def minIndex: Int = 0
    def maxIndex: Int = data.length
    def apply(i: Int) = data(i)
  }
  implicit class SymbolToIntSeries(col: Symbol)(implicit ds: Dataset[_]) extends PlotIntSeries {
    import ds.sparkSession.implicits._
    val data = ds.select(col).as[Int].collect()
    def minIndex: Int = 0
    def maxIndex: Int = data.length
    def apply(i: Int) = data(i)
  }
  implicit class SymbolToDoubleSeries(col: Symbol)(implicit ds: Dataset[_]) extends PlotDoubleSeries {
    import ds.sparkSession.implicits._
    val data = ds.select(col).as[Double].collect()
    def minIndex: Int = 0
    def maxIndex: Int = data.length
    def apply(i: Int) = data(i)
  }
  implicit class SymbolToStringSeries(col: Symbol)(implicit ds: Dataset[_]) extends PlotStringSeries {
    import ds.sparkSession.implicits._
    val data = ds.select(col).as[String].collect()
    def minIndex: Int = 0
    def maxIndex: Int = data.length
    def apply(i: Int) = data(i)
  }

  def ints(ds: Dataset[_], col: Column): PlotIntSeries = {
    import ds.sparkSession.implicits._
    ds.select(col).as[Int].collect()
  }
  def ints[A](ds: Dataset[A], f: A => Int): PlotIntSeries = {
    import ds.sparkSession.implicits._
    ds.map(f).collect()
  }
  def ints[A](rdd: RDD[A], f: A => Int): PlotIntSeries = {
    rdd.map(f).collect()
  }
  def doubles(ds: Dataset[_], col: Column): PlotDoubleSeries = {
    import ds.sparkSession.implicits._
    ds.select(col).as[Double].collect()
  }
  def doubles[A](ds: Dataset[A], f: A => Double): PlotDoubleSeries = {
    import ds.sparkSession.implicits._
    ds.map(f).collect()
  }
  def doubles[A](rdd: RDD[A], f: A => Double): PlotDoubleSeries = {
    rdd.map(f).collect()
  }
  def strings(ds: Dataset[_], col: Column): PlotStringSeries = {
    import ds.sparkSession.implicits._
    ds.select(col).as[String].collect()
  }
  def strings[A](ds: Dataset[A], f: A => String): PlotStringSeries = {
    import ds.sparkSession.implicits._
    ds.map(f).collect()
  }
  def strings[A](rdd: RDD[A], f: A => String): PlotStringSeries = {
    rdd.map(f).collect()
  }
}
