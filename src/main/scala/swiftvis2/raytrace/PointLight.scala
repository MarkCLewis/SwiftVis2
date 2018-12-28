package raytrace

class PointLight(c:FColor,p:Point) extends Light {
  val col=c
  val point=p
    
  override def color(id:IntersectData,geom:Geometry) = {
    val outRay=new Ray(id.point+id.norm*0.0001,point)
    val oid=geom.intersect(outRay)
    oid match {
      case None => {
        val intensity= (outRay.dirVect.normalize dot id.norm).toFloat
        if(intensity<0) new FColor(0,0,0,1) else col*intensity;
      }
      case Some(nid) => {
        if(nid.time<0 || nid.time>1) {
          val intensity= (outRay.dirVect.normalize dot id.norm).toFloat
          if(intensity<0) new FColor(0,0,0,1) else col*intensity;
        } else {
            new FColor(0,0,0,1)
        }
      }
    }
  }
}
