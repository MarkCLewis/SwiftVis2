package swiftvis2.raytrace

case class BoundingBox(min: Point, max: Point) extends Box with Bounds {
  override def movedBy(v: Vect): BoundingBox = BoundingBox(min + v, max + v)

  override def boundingSphere: BoundingSphere = BoundingSphere((min + max.toVect) / 2, (max - min).magnitude / 2)

  override def boundingBox: BoundingBox = this
}

object BoundingBox {
  def mutualBox(b1: Box, b2: Box): BoundingBox = BoundingBox(b1.min min b2.min, b1.max max b2.max)
}