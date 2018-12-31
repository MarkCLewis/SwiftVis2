package swiftvis2.raytrace

case class Ray(p0: Point, dir: Vect) {
  def p1 = p0 + dir
  def point(t: Double): Point = p0 + dir * t;
}

object Ray {
  def apply(p0: Point, p1: Point): Ray = Ray(p0, p1 - p0)
}
