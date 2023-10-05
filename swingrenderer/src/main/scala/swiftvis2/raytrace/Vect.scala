package swiftvis2.raytrace

case class Vect(x: Double, y: Double, z: Double) {
  def +(v: Vect) = Vect(x + v.x, y + v.y, z + v.z)
  def -(v: Vect) = Vect(x - v.x, y - v.y, z - v.z)
  def unary_- = Vect(-x, -y, -z)
  def dot(v: Vect) = x * v.x + y * v.y + z * v.z
  def cross(v: Vect) = Vect(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x)
  def *(s: Double) = Vect(x * s, y * s, z * s)
  def /(s: Double) = Vect(x / s, y / s, z / s)
  def magnitude = Math.sqrt(x * x + y * y + z * z)
  def normalize = {
    val mag = magnitude
    if (mag == 1) this
    else new Vect(x / mag, y / mag, z / mag)
  }
  def min(v: Vect) = Vect(x min v.x, y min v.y, z min v.z)
  def max(v: Vect) = Vect(x max v.x, y max v.y, z max v.z)
  def offsetAll(d: Double) = Vect(x - d, y - d, z - d)
  def maxDim: Int = if(x >= y && x >= z) 0 else if(y >= z) 1 else 2
  def apply(dim: Int): Double = dim match {
    case 0 => x
    case 1 => y
    case 2 => z
  }
  // Spark needs hashing. Let's hope this doesn't break stuff.
  // override def equals(that: Any): Boolean = that match {
  //   case v: Vect => (x-v.x).abs < 1e-8 && (y-v.y).abs < 1e-8 && (z-v.z).abs < 1e-8
  //   case _ => false
  // }
  // override def hashCode(): Int = {
  //   println("Don't hash on Vects!")
  //   Thread.dumpStack()
  //   9802
  // }
}
