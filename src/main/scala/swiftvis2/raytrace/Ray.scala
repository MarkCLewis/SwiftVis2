package raytrace

class Ray(val p0:Point,val p1:Point) {
  def this(p0:Point,v:Vect) = this(p0,p0+v)
  def dirVect : Vect = p1-p0
  def point(t:Double):Point= p0+(p1-p0)*t;
}
