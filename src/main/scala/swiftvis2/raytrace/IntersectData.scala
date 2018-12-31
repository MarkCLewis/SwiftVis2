package swiftvis2.raytrace

import java.awt.Color

case class IntersectData(time: Double, point: Point, norm: Vect, color: RTColor, reflect: Double, geom: Geometry)

object IntersectData {
  def apply(t: Double, p: Point, v: Vect, g: Geometry) = new IntersectData(t, p, v, RTColor(1.0f, 1.0f, 1.0f, 1.0f), 0, g)
}
