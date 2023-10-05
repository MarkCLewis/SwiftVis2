package swiftvis2.raytrace

case class Point(x:Double, y:Double, z:Double) {
  def +(v:Vect) = Point(x+v.x,y+v.y,z+v.z)
  def -(v:Vect) = Point(x-v.x,y-v.y,z-v.z)
  def -(p:Point) = Vect(x-p.x,y-p.y,z-p.z)
  def /(c: Double) = Point(x / c, y / c, z / c)
  def -(c: Double) = Point(x - c, y - c, z - c)
  def +(c: Double) = Point(x + c, y + c, z + c)
  def toVect = Vect(x,y,z)
  def distanceTo(p:Point) = {
    val dx=x-p.x
    val dy=y-p.y
    val dz=z-p.z
    Math.sqrt(dx*dx+dy*dy+dz*dz)
  }
  def min(p: Point) = Point(x min p.x, y min p.y, z min p.z)
  def max(p: Point) = Point(x max p.x, y max p.y, z max p.z)
  def offsetAll(d: Double) = Point(x+d, y+d, z+d)
  def maxDim: Int = if(x >= y && x >= z) 0 else if(y >= z) 1 else 2
  def apply(dim: Int): Double = dim match {
    case 0 => x
    case 1 => y
    case 2 => z
  }
  def updateDim(dim: Int, v: Double): Point = dim match {
    case 0 => Point(v, y, z)
    case 1 => Point(x, v, z)
    case 2 => Point(x, y, v)
  }
  // Spark needs hashing. Let's hope this doesn't break stuff.
  // override def equals(that: Any): Boolean = that match {
  //   case p: Point => (x-p.x).abs < 1e-8 && (y-p.y).abs < 1e-8 && (z-p.z).abs < 1e-8
  //   case _ => false
  // }
  // override def hashCode(): Int = {
  //   println("Don't hash on Points!")
  //   Thread.dumpStack()
  //   9801
  // }
}
