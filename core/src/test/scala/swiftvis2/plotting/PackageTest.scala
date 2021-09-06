package swiftvis2.plotting

import org.scalatest._
import flatspec._
import matchers._

class PackageTest extends AnyFlatSpec with should.Matchers {
  "Double Range" should "have proper elements" in {
    val dr = doubleRange(2.5, 4.5, 0.5)
    // assert(dr.length == 5)
    // assert(dr(0) == 2.5)
    // assert(dr(1) == 3.0)
    // assert(dr(2) == 3.5)
    // assert(dr(3) == 4.0)
    // assert(dr(4) == 4.5)
  }
}