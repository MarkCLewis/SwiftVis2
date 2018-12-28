package raytrace

import java.awt.Color

abstract class Geometry {
  def intersect(r:Ray) : Option[IntersectData]
  def boundingSphere : Sphere
}
