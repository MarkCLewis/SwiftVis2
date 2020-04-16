package swiftvis2.raytrace

trait Bounds {
  def movedBy(v: Vect): Bounds
  def intersectParam(r: Ray): Option[(Double, Vect, Double, Vect)]
  def boundingSphere: BoundingSphere
  def boundingBox: BoundingBox
}

trait BoundsBuilder[B] extends Serializable {
  def fromMinMax(min: Point, max: Point): B
  def mutual(b1: B, b2: B): B
}

object SphereBoundsBuilder extends BoundsBuilder[BoundingSphere] {
  def fromMinMax(min: Point, max: Point): BoundingSphere = {
    val rad = (max - min) / 2
    BoundingSphere(min + rad, rad.magnitude)
  }
  def mutual(b1: BoundingSphere, b2: BoundingSphere): BoundingSphere = {
    BoundingSphere.mutualSphere(b1, b2)
  }
}

object BoxBoundsBuilder extends BoundsBuilder[BoundingBox] {
  def fromMinMax(min: Point, max: Point): BoundingBox = {
    BoundingBox(min, max)
  }
  def mutual(b1: BoundingBox, b2: BoundingBox): BoundingBox = {
    BoundingBox.mutualBox(b1, b2)
  }
}