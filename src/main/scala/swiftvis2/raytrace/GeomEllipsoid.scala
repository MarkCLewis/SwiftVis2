package raytrace

import java.awt.Color

class GeomEllipsoid(c:Point,axis1:Vect,axis2:Vect,axis3:Vect,col:(Point)=>Color,ref:(Point)=>Double) extends Geometry {
  val center=c;
  val r1=axis1.magnitude
  val r2=axis2.magnitude
  val r3=axis3.magnitude
  val a1=axis1.normalize
  val a2=axis2.normalize
  val a3=axis3.normalize
  val mag=r1*r1+r2*r2+r3*r3
  val color=col
  val reflect=ref

  override def intersect(ray:Ray) : Option[IntersectData] = {
    val r=ray.dirVect
    val r0=(ray.p0-center)
    val a=(a1 dot r)*(a1 dot r)/(r1*r1)+(a2 dot r)*(a2 dot r)/(r2*r2)+(a3 dot r)*(a3 dot r)/(r3*r3)
    val b=2*((a1 dot r0)*(a1 dot r)/(r1*r1)+(a2 dot r0)*(a2 dot r)/(r2*r2)+(a3 dot r0)*(a3 dot r)/(r3*r3))
    val c=(a1 dot r0)*(a1 dot r0)/(r1*r1)+(a2 dot r0)*(a2 dot r0)/(r2*r2)+(a3 dot r0)*(a3 dot r0)/(r3*r3)-1
    val root=b*b-4*a*c
    
    if(root<0) None
    else {
      val s1=(-b-Math.sqrt(root))/(2*a)
      val s2=(-b+Math.sqrt(root))/(2*a)
      val s=if(s1<0) s2 else s1
      if(s<0) None
      else {
        val pnt=ray point s
        val sep=(pnt-center).normalize
        val normal=a1*(a1 dot sep)*mag/r1+a2*(a2 dot sep)*mag/r2+a3*(a3 dot sep)*mag/r3
        Some(new IntersectData(s,pnt,normal,color(pnt),reflect(pnt),this))
      }
    }
  }
  
  override def toString():String = {
    "ellipse at "+center
  }
  
  override val boundingSphere : Sphere = new BoundingSphere(center,r1 max r2 max r3)
}
