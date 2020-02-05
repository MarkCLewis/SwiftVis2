package swiftvis2.raytrace

trait Box {
  def min: Point
  def max: Point

  def movedBy(v: Vect): Box

  def intersectParam(r: Ray): Option[(Double, Vect, Double, Vect)] = {
    val dir = r.dir
    val p0 = r.p0
    val txmin = (min.x - p0.x) / dir.x -> Vect(-1, 0, 0)
    val txmax = (max.x - p0.x) / dir.x -> Vect(1, 0, 0)
    val tymin = (min.y - p0.y) / dir.y -> Vect(0, -1, 0)
    val tymax = (max.y - p0.y) / dir.y -> Vect(0, 1, 0)
    val tzmin = (min.z - p0.z) / dir.z -> Vect(0, 0, -1)
    val tzmax = (max.z - p0.z) / dir.z -> Vect(0, 0, 1)

    val (tx1, tx2) = if(txmin._1 < txmax._1) (txmin,txmax) else (txmax,txmin)
    val (ty1, ty2) = if(tymin._1 < tymax._1) (tymin,tymax) else (tymax,tymin)
    val (tz1, tz2) = if(tzmin._1 < tzmax._1) (tzmin,tzmax) else (tzmax,tzmin)

    val inymin = ty1._1 >= tx1._1 && ty1._1 <= tx2._1 && ty1._1 >= tz1._1 && ty1._1 <= tz2._1
    val inymax = ty2._1 >= tx1._1 && ty2._1 <= tx2._1 && ty2._1 >= tz1._1 && ty2._1 <= tz2._1
    val inzmin = tz1._1 >= tx1._1 && tz1._1 <= tx2._1 && tz1._1 >= ty1._1 && tz1._1 <= ty2._1
    val inzmax = tz2._1 >= tx1._1 && tz2._1 <= tx2._1 && tz2._1 >= ty1._1 && tz2._1 <= ty2._1
    val inxmin = tx1._1 >= tz1._1 && tx1._1 <= tz2._1 && tx1._1 >= ty1._1 && tx1._1 <= ty2._1
    val inxmax = tx2._1 >= tz1._1 && tx2._1 <= tz2._1 && tx2._1 >= ty1._1 && tx2._1 <= ty2._1

    val values = Array(inxmin -> tx1, inxmax -> tx2, inymin -> ty1, inymax -> ty2, inzmin -> tz1, inzmax -> tz2).filter(_._1)
    if (values.isEmpty) None 
    else if (values.length == 2) {
      Some(if (values(0)._2._1 <= values(1)._2._1) (values(0)._2._1, values(0)._2._2, values(1)._2._1, values(1)._2._2)
        else (values(1)._2._1, values(1)._2._2, values(0)._2._1, values(0)._2._2))
    } else {
      val svalues = values.sortBy(_._2._1)
      Some(if (svalues.head._2._1 <= svalues.last._2._1) (svalues.head._2._1, svalues.head._2._2, svalues.last._2._1, svalues.last._2._2)
        else (svalues.last._2._1, svalues.last._2._2, svalues.head._2._1, svalues.head._2._2))
    }
  }

}