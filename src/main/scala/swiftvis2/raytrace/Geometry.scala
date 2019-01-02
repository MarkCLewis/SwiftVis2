package swiftvis2.raytrace

import java.awt.Color

trait Geometry {
  def intersect(r:Ray) : Option[IntersectData]
  def boundingSphere : Sphere
}
