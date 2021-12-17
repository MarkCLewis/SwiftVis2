package swiftvis2.raytrace

class GeomPolyFunc(pnts: Array[Point], normal: (Point) => Vect, colors: (Point) => RTColor, reflect: (Point) => Double) extends Geometry {
  val n = ((pnts(2) - pnts(1)) cross (pnts(0) - pnts(1))).normalize

  override def intersect(r: Ray): Option[IntersectData] = {
    val s = ((pnts(0) - r.p0) dot n) / (r.dir dot n)
    if (s < 0) None
    else {
      val pnt = r point s
      val firstSgn = (pnts(0) - pnts(pnts.length - 1)) cross (pnt - pnts(pnts.length - 1)) dot n
      val compSigns = for (i <- 0 until pnts.length - 1)
        yield ((pnts(i + 1) - pnts(i)) cross (pnt - pnts(i)) dot n) * firstSgn
      if (compSigns forall (_ > 0)) {
        val drawNorm = normal(pnt).normalize
        val drawRef = reflect(pnt)
        val color = colors(pnt)
        if (color.a == 0) None else
          Some(new IntersectData(s, pnt, drawNorm, colors(pnt), drawRef, this))
      } else {
        None
      }
    }
  }

  override val boundingSphere: Sphere = {
    val (xmin, xmax, ymin, ymax, zmin, zmax) = ((pnts(0).x, pnts(0).x, pnts(0).y, pnts(0).y, pnts(0).z, pnts(0).z) /: pnts)((b, p) =>
      b match { case (xi, xa, yi, ya, zi, za) => (xi min p.x, xa max p.x, yi min p.y, ya max p.y, zi min p.z, za max p.z) })
    val center = new Point(0.5 * (xmin + xmax), 0.5 * (ymin + ymax), 0.5 * (zmin + zmax))
    val radius = (0.0 /: pnts)((r, p) => (r max (p distanceTo center)))
    new BoundingSphere(center, radius)
  }

  override def boundingBox: Box = {
    BoundingBox(pnts.reduceLeft(_ min _), pnts.reduceLeft(_ max _))
  }
}
