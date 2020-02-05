package swiftvis2.raytrace

import org.scalatest.{FlatSpec, Matchers}

class BoxIntersectTest extends FlatSpec with Matchers {
  "A box" should "intersect basic" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(2, 2.5, 3), Vect(-1, -1, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (false)
    hit.get._1 should be (2.0)
    hit.get._2 should be (Vect(0, 0, 1))
    hit.get._3 should be (3.0)
    hit.get._4 should be (Vect(-1, 0, 0))
  }

  it should "miss basic" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(2, 2.5, 5), Vect(-1, -1, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (true)
  }

  it should "handle a zero dy and hit" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(2, 0, 3), Vect(-1, 0, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (false)
    hit.get._1 should be (2.0)
    hit.get._2 should be (Vect(0, 0, 1))
    hit.get._3 should be (3.0)
    hit.get._4 should be (Vect(-1, 0, 0))
  }

  it should "handle a zero dy and miss" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(2, 0, 4.1), Vect(-1, 0, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (true)
  }

  it should "handle a zero dy and a corner hit" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(2, 0, 4), Vect(-1, 0, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (false)
    hit.get._1 should be (3.0)
    hit.get._2 should be (Vect(-1, 0, 0))
    hit.get._3 should be (3.0)
    hit.get._4 should be (Vect(0, 0, 1))
  }

  it should "handle a zero dy and hit two corners through the middle" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(2, 0, 2), Vect(-1, 0, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (false)
    hit.get._1 should be (1.0)
    Array(hit.get._2) should contain oneOf (Vect(1, 0, 0), Vect(0, 0, 1))
    hit.get._3 should be (3.0)
    Array(hit.get._4) should contain oneOf (Vect(0, 0, -1), Vect(-1, 0, 0))
  }

  it should "handle a hit down an edge" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(0.99999999, 0, 3), Vect(0, 0, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (false)
    hit.get._1 should be (2.0)
    hit.get._2 should be (Vect(0, 0, 1))
    hit.get._3 should be (4.0)
    hit.get._4 should be (Vect(0, 0, -1))
  }

  it should "handle a miss down an edge" in {
    val box = BoundingBox(Point(-1, -1, -1), Point(1, 1, 1))
    val ray = Ray(Point(1.2, 0, 3), Vect(0, 0, -1))
    val hit = box.intersectParam(ray)
    hit.isEmpty should be (true)
  }
}