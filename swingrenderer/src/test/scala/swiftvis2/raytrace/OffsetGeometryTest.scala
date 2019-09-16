package swiftvis2.raytrace

import org.scalatest.{FlatSpec, Matchers}

class OffsetGeometryTest extends FlatSpec with Matchers {
  "OffsetGeometry" should "have the correct simple intercept" in {
    val sphere = GeomSphere(Point(0,0,0), 1, p => RTColor.White, p => 0)
    val offsetSphere = OffsetGeometry(sphere, Vect(1,1,0))
    val hit = offsetSphere.intersect(Ray(Point(1, 1, -5), Vect(0, 0, 1)))
    hit.nonEmpty should be (true)
    hit.get.time should be (4.0)
    hit.get.point should be (Point(1, 1, -1))
  }

  it should "have proper intersect data for a large random test outside" in {
    val spheres = Array.fill(2000)(GeomSphere(Point(math.random, math.random, math.random), 0.01, p => RTColor.White, p => 0.0))
    val offset = Vect(1, 1, 1)
    val olist = new OffsetGeometry(new ListScene(spheres:_*), offset)
    val list = new ListScene(spheres.map(gs => gs.copy(center = gs.center + offset)):_*)
    
    // Hits first
    for(s <- spheres) {
      val ray = Ray(Point(100, 0, 0), s.center)
      olist.intersect(ray) should be (list.intersect(ray))
    }
    
    // Random
    for(_ <- 1 to 100) {
      val ray = Ray(Point(100, 0, 0), Point(math.random, math.random, math.random))
      olist.intersect(ray) should be (list.intersect(ray))
    }
  }
}