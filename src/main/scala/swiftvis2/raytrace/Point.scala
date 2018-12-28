package raytrace

case class Point(x:Double,y:Double,z:Double) {
  def +(v:Vect) = Point(x+v.x,y+v.y,z+v.z)
  def -(v:Vect) = Point(x-v.x,y-v.y,z-v.z)
  def -(p:Point) = Vect(x-p.x,y-p.y,z-p.z)
  def toVect = Vect(x,y,z)
  def distanceTo(p:Point) = {
    val dx=x-p.x
    val dy=y-p.y
    val dz=z-p.z
    Math.sqrt(dx*dx+dy*dy+dz*dz)
  }
  override def toString : String = "Point : "+x+" "+y+" "+z
}
