package swiftvis2.raytrace

case class DirectionLight(col: RTColor, v: Vect) extends Light {
  val dir = v.normalize

  override def color(id: IntersectData, geom: Geometry) = {
    val outRay = Ray(id.point + id.norm * 0.0001, id.point - dir * 1000000.0)
    val oid = geom.intersect(outRay)
    oid match {
      case None => {
        val intensity = -(dir dot id.norm)
        if (intensity < 0) new RTColor(0, 0, 0, 1) else col * intensity;
      }
      case Some(nid) => {
        //println(this+" hit "+nid.time+" "+nid.point+" "+nid.norm+" "+nid.color+" "+nid.geom);
        new RTColor(0, 0, 0, 1)
      }
    }
  }
}
