package raytrace

import java.awt.Color

class GeomPolygon(pnts:Array[Point],normals:Array[Vect],colors:(Point)=>Color,reflect:Array[Double]) extends Geometry {
  val t=pnts
  val n=(t(2)-t(1)) cross (t(0)-t(1)) normalize
  val cols=colors
  val norms=normals map (_ normalize)
  val ref=reflect
  
  def this(pnts:Array[Point],colors:(Point)=>Color,reflect:Array[Double]) = 
    this(pnts,Array.tabulate(pnts.length)((x)=>(pnts(2)-pnts(1)) cross (pnts(0)-pnts(1)) normalize),colors,reflect)
  
  override def intersect(r:Ray) : Option[IntersectData] = {
    val s=((t(0)-r.p0) dot n)/(r.dirVect dot n)
    if(s<0) None
    else {
      val pnt=r point s
      val firstSgn=(t(0)-t(t.length-1)) cross (pnt-t(t.length-1)) dot n
      val compSigns=for(i <- 0 until t.length-1) 
        yield ((t(i+1)-t(i)) cross (pnt-t(i)) dot n)*firstSgn
      if(compSigns forall (_>0)) {
        val weights=calcWeights(pnt)
        val drawNorm=(new Vect(0,0,0) /: (norms zip weights)) ((v,p)=>p match {case (n,w) => v+n*w})
        val drawRef=(0.0 /: (ref zip weights)) ((v,p)=>p match {case (r,w) => v+r*w})
        Some(new IntersectData(s,pnt,drawNorm,cols(pnt),drawRef,this))
      } else {
        None
      }
    }
  }

  override val boundingSphere : Sphere = {
    val (xmin,xmax,ymin,ymax,zmin,zmax)=((t(0).x,t(0).x,t(0).y,t(0).y,t(0).z,t(0).z) /: t)((b,p)=>
        b match { case (xi,xa,yi,ya,zi,za) => (xi min p.x,xa max p.x,yi min p.y,ya max p.y,zi min p.z,za max p.z) })
    val center=new Point(0.5*(xmin+xmax),0.5*(ymin+ymax),0.5*(zmin+zmax))
    val radius=(0.0 /: t)((r,p)=>(r max (p distanceTo center)))
    new BoundingSphere(center,radius)
  }
  
  private def calcWeights(loc:Point):Array[Double] = {
    val legFrac=Array(0.0,0.0,0.0)
    val legWeight=Array(0.0,0.0,0.0)
    val ret=Array(0.0,0.0,0.0)
    var tot=0.0
    for(i <- 1 until 3) {
      val oi=(i+1)%3
      var leg=pnts(oi)-pnts(i)
      val legLen=leg.magnitude
      leg=leg.normalize
      val toLoc=loc-pnts(i)
      val len=leg dot toLoc
      legFrac(i)=1-len/legLen
      val legPnt=pnts(i)+leg*len
      val dist=(loc-legPnt).magnitude
      if(dist==0) {
        ret(i)=legFrac(i);
        ret(oi)=1-legFrac(i);
        return ret
      }
      legWeight(i)=1/dist;
      tot+=legWeight(i)
    }
    for(i <- 1 until 3) {
      val oi=(i+1)%3
      legWeight(i)/=tot
      ret(i)+=legWeight(i)*legFrac(i)
      ret(oi)+=legWeight(i)*(1-legFrac(i))
    }
    ret
  }

}
