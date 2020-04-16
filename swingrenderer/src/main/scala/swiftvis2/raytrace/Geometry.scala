package swiftvis2.raytrace

trait Geometry extends Serializable {
  def intersect(r:Ray) : Option[IntersectData]
  def boundingSphere : Sphere
  def boundingBox: Box
}
