package swiftvis2.plotting

import org.scalatest.FlatSpec

class AxisTest extends FlatSpec {
  def fixture = new {
    val axis = NumericAxis(Some(0), Some(100), tickSpacing = Some(2))
    val logAxis = NumericAxis(Some(0), Some(100), tickSpacing = Some(1), style = Axis.ScaleStyle.LogDense)
    val sparseLogAxis = NumericAxis(Some(0), Some(100), tickSpacing = Some(1), style = Axis.ScaleStyle.LogSparse)
  }
  
  "A linear axis" should "do conversions" in {
    val f = fixture
    val conv = f.axis.toPixelFunc(0, 400, -10, 10)
    assert(conv(-10.0) == 0)
    assert(conv(0.0) == 200)
    assert(conv(10.0) == 400)
  }
  
  "A log axis" should "do conversions" in {
    val f = fixture
    val conv = f.logAxis.toPixelFunc(0, 400, 1, 100)
    assert(conv(1.0) == 0)
    assert(conv(10.0) == 200)
    assert(conv(100.0) == 400)
  }
  
  "A linear axis" should "have proper ticks" in {
    val f = fixture
    val locs = f.axis.calcTickLocations(-10, 10)
    assert(locs == Seq(-10.0, -8.0, -6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0))
    val locs2 = f.axis.calcTickLocations(-7, 11)
    assert(locs2 == Seq(-6.0, -4.0, -2.0, 0.0, 2.0, 4.0, 6.0, 8.0, 10.0))
  }
  
  "A dense log axis" should "have proper ticks" in {
    val f = fixture
    val locs = f.logAxis.calcTickLocations(1, 100)
    assert(locs == Seq(1.0, 2.0, 4.0, 6.0, 8.0, 10.0, 20.0, 40.0, 60.0, 80.0, 100.0))
  }
  
  "A sparse log axis" should "have proper ticks" in {
    val f = fixture
    val locs = f.sparseLogAxis.calcTickLocations(0.1, 1000)
    assert(locs == Seq(0.1, 1.0, 10.0, 100.0, 1000.0))
  }
}