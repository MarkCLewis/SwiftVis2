package swiftvis2.raytrace

abstract class Light {
    def color(id:IntersectData,geom:Geometry):RTColor
}
