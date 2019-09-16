package swiftvis2.raytrace

class GeomPolyFunc(pnts: Array[Point], normal: (Point) => Vect, colors: (Point) => RTColor, reflect: (Point) => Double) extends Geometry {
  val t = pnts
  val n = (t(2) - t(1)) cross (t(0) - t(1)) normalize
  val cols = colors
  val ref = reflect

  override def intersect(r: Ray): Option[IntersectData] = {
    val s = ((t(0) - r.p0) dot n) / (r.dir dot n)
    if (s < 0) None
    else {
      val pnt = r point s
      val firstSgn = (t(0) - t(t.length - 1)) cross (pnt - t(t.length - 1)) dot n
      val compSigns = for (i <- 0 until t.length - 1)
        yield ((t(i + 1) - t(i)) cross (pnt - t(i)) dot n) * firstSgn
      if (compSigns forall (_ > 0)) {
        val drawNorm = normal(pnt).normalize
        val drawRef = ref(pnt)
        val color = cols(pnt)
        if (color.a == 0) None else
          Some(new IntersectData(s, pnt, drawNorm, cols(pnt), drawRef, this))
      } else {
        None
      }
    }
  }

  override val boundingSphere: Sphere = {
    val (xmin, xmax, ymin, ymax, zmin, zmax) = ((t(0).x, t(0).x, t(0).y, t(0).y, t(0).z, t(0).z) /: t)((b, p) =>
      b match { case (xi, xa, yi, ya, zi, za) => (xi min p.x, xa max p.x, yi min p.y, ya max p.y, zi min p.z, za max p.z) })
    val center = new Point(0.5 * (xmin + xmax), 0.5 * (ymin + ymax), 0.5 * (zmin + zmax))
    val radius = (0.0 /: t)((r, p) => (r max (p distanceTo center)))
    new BoundingSphere(center, radius)
  }
}
