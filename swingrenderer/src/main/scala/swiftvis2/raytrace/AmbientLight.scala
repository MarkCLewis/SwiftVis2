package swiftvis2.raytrace

case class AmbientLight(c:RTColor) extends Light {
    override def color(id:IntersectData,geom:Geometry)=c
}
