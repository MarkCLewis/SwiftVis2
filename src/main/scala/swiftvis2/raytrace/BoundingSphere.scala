package swiftvis2.raytrace

case class BoundingSphere(center:Point, radius:Double) extends Sphere

object BoundingSphere {
  def mutualSphere(s1:Sphere,s2:Sphere):Sphere = {
    val sep=s1.center-s2.center
    val sepm=sep.magnitude
    if(s1.radius>=sepm+s2.radius) s1
    else if(s2.radius>=sepm+s1.radius) s2
    else {
      val radius=(sepm+s1.radius+s2.radius)*0.5
      val center=s1.center+sep.normalize*(radius-s1.radius)
      new BoundingSphere(center,radius)
    }
  }
}
