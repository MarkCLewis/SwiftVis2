package raytrace

import java.awt.Color

class GeomSphere(val center:Point,val radius:Double,col:(Point)=>Color,ref:(Point)=>Double) extends Geometry with Sphere {
    val color=col
    val reflect=ref

    override def intersect(r:Ray) : Option[IntersectData] = {
      val interPair=intersectParam(r)
      val inter=if(interPair._1<0) interPair._2 else interPair._1
      if(inter<0) None
      else {
        val pnt=r point inter
        val normal=(pnt-center).normalize
        Some(new IntersectData(inter,pnt,normal,color(pnt),reflect(pnt),this))
      }
    }
    
    override def boundingSphere : Sphere = this

}
