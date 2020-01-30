package swiftvis2.raytrace

case class BoundingSphere(center:Point, radius:Double) extends Sphere with Bounds {
  def movedBy(v: Vect): BoundingSphere = copy(center = center+v)

  override def boundingSphere: BoundingSphere = this

  override def boundingBox: BoundingBox = BoundingBox(center - radius, center + radius)
}

object BoundingSphere {
  def mutualSphere(s1:Sphere,s2:Sphere):BoundingSphere = {
    val sep=s1.center-s2.center
    val sepm=sep.magnitude
    if(s1.radius>=sepm+s2.radius) BoundingSphere(s1.center, s1.radius)
    else if(s2.radius>=sepm+s1.radius) BoundingSphere(s2.center, s2.radius)
    else {
      val radius=(sepm+s1.radius+s2.radius)*0.5
      val center=s1.center+sep.normalize*(radius-s1.radius)
      new BoundingSphere(center,radius)
    }
  }
}
