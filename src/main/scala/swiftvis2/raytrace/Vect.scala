package raytrace

case class Vect(x:Double,y:Double,z:Double) {
    def +(v:Vect) = Vect(x+v.x,y+v.y,z+v.z)
    def -(v:Vect) = Vect(x-v.x,y-v.y,z-v.z)
    def dot(v:Vect) = x*v.x+y*v.y+z*v.z
    def cross(v:Vect) = Vect(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x)
    def *(s:Double) = Vect(x*s,y*s,z*s)
    def /(s:Double) = Vect(x/s,y/s,z/s)
    def magnitude = Math.sqrt(x*x+y*y+z*z)
    def normalize = {
      val mag=magnitude
      if(mag==1) this
      else new Vect(x/mag,y/mag,z/mag)
    }
    override def toString : String = "Vect : "+x+" "+y+" "+z
}
