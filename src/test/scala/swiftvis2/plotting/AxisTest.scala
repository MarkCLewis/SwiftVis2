package swiftvis2.plotting

import org.scalatest.FlatSpec

class AxisTest extends FlatSpec {
  def nfixture = new {
    val axis = NumericAxis(Some(0), Some(100))
  }
  
  // TODO - test conversion functions
  // TODO - test getting tick markers
}