package swiftvis2.raytrace

trait Sphere {
  val center: Point
  val radius: Double

  def intersectParam(r: Ray): Option[(Double, Double)] = {
    val dr = r.dir
    val dc = center - r.p0
    val a = dr dot dr
    val b = -2 * (dr dot dc)
    val c = (dc dot dc) - radius * radius
    val root = b * b - 4 * a * c
    if (root < 0) None
    else {
      val sroot = Math.sqrt(root)
      Some((-b - sroot) / (2 * a), (-b + sroot) / (2 * a))
    }
  }
}

object Sphere {
  def smallerPositiveParam(param: (Double, Double)): Double = {
    val (enter, exit) = param
    if (enter < 0) exit else enter
  }
}
