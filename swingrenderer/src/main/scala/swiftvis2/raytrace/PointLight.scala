package swiftvis2.raytrace

case class PointLight(col: RTColor, point: Point, unlitGeom: Set[Geometry] = Set.empty) extends Light {
  override def color(id: IntersectData, geom: Geometry) = {
    if (unlitGeom(id.geom)) RTColor(0, 0, 0, 1) else {
      val outRay = Ray(id.point + id.norm * 0.0001 * id.geom.boundingSphere.radius, point)
      val oid = geom.intersect(outRay)
      oid match {
        case None => {
          val intensity = (outRay.dir.normalize dot id.norm).toFloat
          if (intensity < 0) new RTColor(0, 0, 0, 1) else col * intensity;
        }
        case Some(nid) => {
          if (nid.time < 0 || nid.time > 1) {
            val intensity = (outRay.dir.normalize dot id.norm).toFloat
            if (intensity < 0) new RTColor(0, 0, 0, 1) else col * intensity;
          } else {
            new RTColor(0, 0, 0, 1)
          }
        }
      }
    }
  }
}
