package raytrace

trait Sphere {
    val center : Point
    val radius : Double
    
    def intersectParam(r:Ray) : (Double,Double) = {
      val dr:Vect=r dirVect
      val dc:Vect=center - r.p0
      val a=dr dot dr
      val b= -2*(dr dot dc)
      val c=(dc dot dc)-radius*radius
      val root=b*b-4*a*c
      if(root<0) (-1,-1)
      else ((-b-Math.sqrt(root))/(2*a),(-b+Math.sqrt(root))/(2*a))
    }
}
