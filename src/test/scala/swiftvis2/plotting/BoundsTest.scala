package swiftvis2.plotting

import org.scalatest.FlatSpec

class BoundsTest extends FlatSpec {
  def fixture = new {
    val bounds = Bounds(0, 0, 10, 5) 
  }
  "A bounds" should "have appropriate center" in {
    val f = fixture
    assert(f.bounds.centerX == 5)
    assert(f.bounds.centerY == 2.5)
  }
  
  it should "appropriately subX" in {
    val f = fixture
    val b2 = f.bounds.subX(0.25, 0.75)
    assert(b2.x == 2.5)
    assert(b2.width == 5)
    assert(b2.y == 0)
    assert(b2.height == 5)
  }

  it should "appropriately subY" in {
    val f = fixture
    val b2 = f.bounds.subY(0.25, 0.75)
    assert(b2.x == 0)
    assert(b2.width == 10)
    assert(b2.y == 1.25)
    assert(b2.height == 2.5)
  }
}