package swiftvis2.raytrace

case class GeomBox(
  min: Point, max: Point, 
  color: (Point) => RTColor, reflect: (Point) => Double) extends Geometry with Box {

  def movedBy(v: Vect): Box = copy(min = min + v, max = max + v)
  
  override def intersect(r: Ray): Option[IntersectData] = {
    intersectParam(r).flatMap { case (enter, enterNorm, exit, exitNorm) =>
      val (inter, normal) = if (enter < 0) (exit, exitNorm) else (enter, enterNorm)
      if (inter < 0) None
      else {
        val pnt = r point inter
        Some(new IntersectData(inter, pnt, normal, color(pnt), reflect(pnt), this))
      }
    }
  }

  override def boundingSphere: Sphere = {
    val center = (max + min.toVect) / 2
    BoundingSphere(center, (min - center).magnitude)
  }

  override def boundingBox: Box = this
}