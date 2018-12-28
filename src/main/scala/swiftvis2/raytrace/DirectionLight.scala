package raytrace

class DirectionLight(c:FColor,v:Vect) extends Light {
  val col=c
  val dir=v.normalize
    
  override def color(id:IntersectData,geom:Geometry) = {
    val outRay=new Ray(id.point+id.norm*0.0001,id.point-dir*1000)
    val oid=geom.intersect(outRay)
    oid match {
      case None => {
        val intensity= -((dir dot id.norm).toFloat)
        if(intensity<0) new FColor(0,0,0,1) else col*intensity;
      }
      case Some(nid) => {
        //println(this+" hit "+nid.time+" "+nid.point+" "+nid.norm+" "+nid.color+" "+nid.geom);
        new FColor(0,0,0,1)
      }
    }
  }
}
