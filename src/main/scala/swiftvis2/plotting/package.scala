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
  
  def argb(a: Double, r: Double, g: Double, b: Double): Int = {
    val ai = (a*255).toInt max 0 min 255
    val ri = (r*255).toInt max 0 min 255
    val gi = (g*255).toInt max 0 min 255
    val bi = (b*255).toInt max 0 min 255
    ai << 24 | ri << 16 | gi << 8 | bi
  }
  
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
  
  implicit class ArrayToStringSeries(data: Array[String]) extends PlotStringSeries {
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