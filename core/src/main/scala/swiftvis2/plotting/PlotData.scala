package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer.FontData

trait PlotSeries extends (Int => Any) {
  def minIndex: Int
  def maxIndex: Int
}

trait PlotDoubleSeries extends PlotSeries with (Int => Double)

trait PlotIntSeries extends PlotSeries with (Int => Int)

trait PlotStringSeries extends PlotSeries with (Int => String)

trait PlotFontSeries extends PlotSeries with (Int => FontData)

object UnboundDoubleSeries extends PlotDoubleSeries {
  def minIndex = Int.MinValue
  def maxIndex = Int.MaxValue
  def apply(i: Int) = throw new UnsupportedOperationException("Attempted to index unbound double series.")
}

object UnboundIntSeries extends PlotIntSeries {
  def minIndex = Int.MinValue
  def maxIndex = Int.MaxValue
  def apply(i: Int) = throw new UnsupportedOperationException("Attempted to index unbound int series.")
}

object UnboundStringSeries extends PlotStringSeries {
  def minIndex = Int.MinValue
  def maxIndex = Int.MaxValue
  def apply(i: Int) = throw new UnsupportedOperationException("Attempted to index unbound string series.")
}
