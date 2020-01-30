package swiftvis2.raytrace

case class OffsetGeometry(original: Geometry, offset: Vect) extends Geometry {
  def intersect(r:Ray) : Option[IntersectData] = {
    original.intersect(r.copy(p0 = r.p0 - offset)).map { id =>
      id.copy(point = id.point + offset)
    }
  }
  def boundingSphere : Sphere = original.boundingSphere.movedBy(offset)

  def boundingBox : Box = original.boundingBox.movedBy(offset)
}