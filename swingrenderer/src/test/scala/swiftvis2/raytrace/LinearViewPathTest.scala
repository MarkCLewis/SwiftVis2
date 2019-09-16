package swiftvis2.raytrace

import org.scalatest.{FlatSpec, Matchers}
import swiftvis2.raytrace.LinearViewPath.{StopPoint, View}

class LinearViewPathTest extends FlatSpec with Matchers {

  "A linear view path" should "adjust the location in apply" in {
    val path = LinearViewPath(List(StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)), 0.1), StopPoint(View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)), 0.1)), List(1.0))
    path(0.05) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(0.6) should be (View(Point(0.5, 1, 2), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(1.15) should be (View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)))
  }
  
  it should "adjust the direction in apply" in {
    val path = LinearViewPath(List(StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)), 0.1), StopPoint(View(Point(0, 0, 0), Vect(0, 1, 0), Vect(0, 1, 0)), 0.1)), List(1.0))
    path(0.05) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(0.6) should be (View(Point(0, 0, 0), Vect(0.5, 0.5, 0).normalize, Vect(0, 1, 0)))
    path(1.15) should be (View(Point(0, 0, 0), Vect(0, 1, 0), Vect(0, 1, 0)))
  }

  it should "adjust the up in apply" in {
    val path = LinearViewPath(List(StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)), 0.1), StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 0, 1)), 0.1)), List(1.0))
    path(0.05) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(0.6) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 0.5, 0.5).normalize))
    path(1.15) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 0, 1)))
  }

  it should "adjust the location and direction in apply" in {
    val path = LinearViewPath(List(StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)), 0.25), 
                                   StopPoint(View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)), 0.25),
                                   StopPoint(View(Point(1, 2, 4), Vect(0, 1, 0), Vect(0, 1, 0)), 0.25),
                                   StopPoint(View(Point(1, 1, 1), Vect(0, 0, 1), Vect(0, 1, 0)), 0.25)), 
                              List(0.75, 0.75, 0.75))
    path(0.05) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(0.625) should be (View(Point(0.5, 1, 2), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(1.05) should be (View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)))
    path(1.625) should be (View(Point(1, 2, 4), Vect(0.5, 0.5, 0).normalize, Vect(0, 1, 0)))
    path(2.05) should be (View(Point(1, 2, 4), Vect(0, 1, 0), Vect(0, 1, 0)))
    path(2.625) should be (View(Point(1, 1.5, 2.5), Vect(0, 0.5, 0.5).normalize, Vect(0, 1, 0)))
    path(3.05) should be (View(Point(1, 1, 1), Vect(0, 0, 1), Vect(0, 1, 0)))
  }
  
  it should "adjust the location and direction with intervals" in {
    val path = LinearViewPath(List(StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)), 0.25), 
                                   StopPoint(View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)), 0.25),
                                   StopPoint(View(Point(1, 2, 4), Vect(0, 1, 0), Vect(0, 1, 0)), 0.25),
                                   StopPoint(View(Point(1, 1, 1), Vect(0, 0, 1), Vect(0, 1, 0)), 0.25)), 
                              List(0.75, 0.75, 0.75))
    val stops = path.atIntervals(0.125)
    stops.length should be (3.25/0.125 + 1)
    stops(0) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(1) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(2) should be (View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(3) should be (View(Point(1.0/6, 2.0/6, 4.0/6), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(4) should be (View(Point(2*1.0/6, 2*2.0/6, 2*4.0/6), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(5) should be (View(Point(3*1.0/6, 3*2.0/6, 3*4.0/6), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(6) should be (View(Point(4*1.0/6, 4*2.0/6, 4*4.0/6), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(7) should be (View(Point(5*1.0/6, 5*2.0/6, 5*4.0/6), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(8) should be (View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(9) should be (View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(10) should be (View(Point(1, 2, 4), Vect(1, 0, 0), Vect(0, 1, 0)))
    stops(11) should be (View(Point(1, 2, 4), Vect(5*1.0/6, 1.0/6, 0).normalize, Vect(0, 1, 0)))
    stops(12) should be (View(Point(1, 2, 4), Vect(4*1.0/6, 2*1.0/6, 0).normalize, Vect(0, 1, 0)))
    stops(13) should be (View(Point(1, 2, 4), Vect(3*1.0/6, 3*1.0/6, 0).normalize, Vect(0, 1, 0)))
    stops(14) should be (View(Point(1, 2, 4), Vect(2*1.0/6, 4*1.0/6, 0).normalize, Vect(0, 1, 0)))
    stops(15) should be (View(Point(1, 2, 4), Vect(1.0/6, 5*1.0/6, 0).normalize, Vect(0, 1, 0)))
    stops(16) should be (View(Point(1, 2, 4), Vect(0, 1, 0), Vect(0, 1, 0)))
    stops(17) should be (View(Point(1, 2, 4), Vect(0, 1, 0), Vect(0, 1, 0)))
    stops(18) should be (View(Point(1, 2, 4), Vect(0, 1, 0), Vect(0, 1, 0)))
    stops(19) should be (View(Point(1, 2 - 1.0/6, 4 - 3.0/6), Vect(0, 5*1.0/6, 1.0/6).normalize, Vect(0, 1, 0)))
    stops(20) should be (View(Point(1, 2 - 2*1.0/6, 4 - 2*3.0/6), Vect(0, 4*1.0/6, 2*1.0/6).normalize, Vect(0, 1, 0)))
    stops(21) should be (View(Point(1, 2 - 3*1.0/6, 4 - 3*3.0/6), Vect(0, 3*1.0/6, 3*1.0/6).normalize, Vect(0, 1, 0)))
    stops(22) should be (View(Point(1, 2 - 4*1.0/6, 4 - 4*3.0/6), Vect(0, 2*1.0/6, 4*1.0/6).normalize, Vect(0, 1, 0)))
    stops(23) should be (View(Point(1, 2 - 5*1.0/6, 4 - 5*3.0/6), Vect(0, 1.0/6, 5*1.0/6).normalize, Vect(0, 1, 0)))
    stops(24) should be (View(Point(1, 1, 1), Vect(0, 0, 1), Vect(0, 1, 0)))
    stops(25) should be (View(Point(1, 1, 1), Vect(0, 0, 1), Vect(0, 1, 0)))
    stops(26) should be (View(Point(1, 1, 1), Vect(0, 0, 1), Vect(0, 1, 0)))
  }
}