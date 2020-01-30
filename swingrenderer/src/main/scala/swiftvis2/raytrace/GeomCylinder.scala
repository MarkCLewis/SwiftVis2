package swiftvis2.raytrace

class GeomCylinder(c0: Point, c1: Point, radius: Double, color: (Point) => RTColor, reflect: (Point) => Double) extends Geometry {
  val l = c1 - c0

  override def intersect(r: Ray): Option[IntersectData] = {
    val A = ((r.p0 - c0) dot l) / (l dot l)
    val B = (r.dir dot l) / (l dot l)
    val D = c0 + l * A - r.p0
    val E = l * B - r.dir

    val a = E dot E
    val b = 2 * (D dot E)
    val c = (D dot D) - radius * radius
    val root = b * b - 4 * a * c
    if (root < 0) None
    else {
      val s1 = (-b - Math.sqrt(root)) / (2 * a)
      val t1 = A + B * s1
      val s2 = (-b + Math.sqrt(root)) / (2 * a)
      val t2 = A + B * s2
      if ((s1 < 0 && s2 < 0) || (t1 < 0 && t2 < 0) || (t1 > 1 && t2 > 1)) None
      else {
        if ((t1 > 0 && t1 < 1 && t2 > 0 && t2 < 1) || (s1 > 0 && t1 > 0 && t1 < 1) || (s2 > 0 && s1 < 0 && t2 > 0 && t2 < 1)) {
          val s = if (s1 < 0) s2 else s1
          val t = if (s1 < 0) t2 else t1
          val pnt = r point s
          val norm = pnt - (c0 + (c1 - c0) * t)
          Some(new IntersectData(s, pnt, norm, color(pnt), reflect(pnt), this))
        } else {
          val (t, norm) = if (t1 < t2) {
            (0, l * -1)
          } else {
            (1, l)
          }
          val s = (t - A) / B
          val pnt = r point s
          Some(new IntersectData(s, pnt, norm, color(pnt), reflect(pnt), this))
        }
      }
    }
  }

  override val boundingSphere: Sphere = new BoundingSphere(c0 + l * 0.5, Math.sqrt((l dot l) + radius * radius))

  // This current implementation gives an overly conservative box, but it is easier and faster than getting a more accurate bounds.
  override def boundingBox: Box = {
    BoundingBox((c0 min c1) - radius, (c0 max c1) + radius) 
  }
}
