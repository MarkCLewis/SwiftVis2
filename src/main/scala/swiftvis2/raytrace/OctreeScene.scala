package swiftvis2.raytrace

class OctreeScene(c: Point, s: Double) extends Geometry with Scene {
  val center = c
  val size = s
  private var children: Array[OctreeScene] = null
  private var geomList: List[Geometry] = Nil

  override def addGeom(geom: Geometry) {
    val qs = size * 0.25
    if (geom.boundingSphere.radius > qs) geomList = geom :: geomList
    else {
      if (children == null) {
        val hs = size * 0.5
        children = Array(new OctreeScene(c + new Vect(-qs, -qs, -qs), hs), new OctreeScene(c + new Vect(qs, -qs, -qs), hs),
          new OctreeScene(c + new Vect(-qs, qs, -qs), hs), new OctreeScene(c + new Vect(qs, qs, -qs), hs),
          new OctreeScene(c + new Vect(-qs, -qs, qs), hs), new OctreeScene(c + new Vect(qs, -qs, qs), hs),
          new OctreeScene(c + new Vect(-qs, qs, qs), hs), new OctreeScene(c + new Vect(qs, qs, qs), hs))
      }
      children(childNum(geom)).addGeom(geom)
    }
  }

  private def childNum(geom: Geometry): Int = {
    (if (geom.boundingSphere.center.x > center.x) 1 else 0) +
      (if (geom.boundingSphere.center.y > center.y) 2 else 0) +
      (if (geom.boundingSphere.center.z > center.z) 4 else 0)
  }

  override def intersect(r: Ray): Option[IntersectData] = {
    val geomOID = ((None: Option[IntersectData]) /: geomList)((oid, g) => {
      val goid = g.intersect(r)
      (oid, goid) match {
        case (None, _)             => goid
        case (_, None)             => oid
        case (Some(id), Some(gid)) => if (gid.time > 0 && gid.time < id.time) goid else oid
      }
    })
    val childOID = if (children == null) None else children.foldLeft(None: Option[IntersectData])((oid, oct) => {
      oct.boundingSphere.intersectParam(r).flatMap {
        case (_, exit) =>
          if (exit < 0) oid
          else {
            val goid = oct.intersect(r)
            (oid, goid) match {
              case (None, _)             => goid
              case (_, None)             => oid
              case (Some(id), Some(gid)) => if (gid.time > 0 && gid.time < id.time) goid else oid
            }
          }
      }
    })
    (geomOID, childOID) match {
      case (None, _)              => childOID
      case (_, None)              => geomOID
      case (Some(gid), Some(cid)) => if (gid.time < cid.time) geomOID else childOID
    }
  }

  override val boundingSphere: Sphere = new BoundingSphere(center, size * 0.5 * (1 + Math.sqrt(3)))
}
