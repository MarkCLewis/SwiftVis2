package swiftvis2

package object plotting {
  val BlackARGB = 0xff000000
  val WhiteARGB = 0xffffffff
  
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

  implicit class ArrayIntToDoubleSeries(data: Array[Int]) extends PlotDoubleSeries {
    def minIndex: Int = 0
    def maxIndex: Int = data.size
    def apply(i: Int): Double = data(i)
  }

  implicit class SeqToIntSeries(data: Seq[Int]) extends PlotIntSeries {
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
  
  // TODO - other implicits
}