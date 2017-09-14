package swiftvis2

/**
 * Helpful constants and implicit conversions.
 */
package object plotting {
  val BlackARGB = 0xff000000
  val WhiteARGB = 0xffffffff
  val RedARGB = 0xffff0000
  val GreenARGB = 0xff00ff00
  val BlueARGB = 0xff0000ff
  val YellowARGB = 0xffffff00
  val CyanARGB = 0xff00ffff
  val MagentaARGB = 0xffff00ff
  
  implicit class SeqToDoubleSeries(data: Seq[Double]) extends PlotDoubleSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int) = data(i)
  }

  implicit class ArrayToDoubleSeries(data: Array[Double]) extends PlotDoubleSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int) = data(i)
  }

  implicit class SeqIntToDoubleSeries(data: Seq[Int]) extends PlotDoubleSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int): Double = data(i)
  }

  implicit class ArrayIntToDoubleSeries(data: Array[Int]) extends PlotDoubleSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int): Double = data(i)
  }

  implicit class ArrayLongToDoubleSeries(data: Array[Long]) extends PlotDoubleSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int): Double = data(i)
  }

  implicit class SeqToIntSeries(data: Seq[Int]) extends PlotIntSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int) = data(i)
  }

  implicit class ArrayToIntSeries(data: Array[Int]) extends PlotIntSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int) = data(i)
  }

  implicit class SeqToStringSeries(data: Seq[String]) extends PlotStringSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int) = data(i)
  }
  
  implicit class DoubleToDoubleSeries(x: Double) extends PlotDoubleSeries {
    def minIndex: Int = Int.MinValue
    def maxIndex: Int = Int.MaxValue
    def apply(i: Int) = x
  }
  
  implicit class IntToIntSeries(j: Int) extends PlotIntSeries {
    def minIndex: Int = Int.MinValue
    def maxIndex: Int = Int.MaxValue
    def apply(i: Int) = j
  }
  
  implicit class DoubleFuncToDoubleSeries(f: Int => Double) extends PlotDoubleSeries {
    def minIndex: Int = Int.MinValue
    def maxIndex: Int = Int.MaxValue
    def apply(i: Int) = f(i)
  }
  
  implicit class IntFuncToIntSeries(f: Int => Int) extends PlotIntSeries {
    def minIndex: Int = Int.MinValue
    def maxIndex: Int = Int.MaxValue
    def apply(i: Int) = f(i)
  }
  
  // TODO - other implicits
}