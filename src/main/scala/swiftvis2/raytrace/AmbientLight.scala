package raytrace

class AmbientLight(c:FColor) extends Light {
    override def color(id:IntersectData,geom:Geometry)=c
}
