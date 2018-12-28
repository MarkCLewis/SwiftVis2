package raytrace

abstract class Light {
    def color(id:IntersectData,geom:Geometry):FColor
}
