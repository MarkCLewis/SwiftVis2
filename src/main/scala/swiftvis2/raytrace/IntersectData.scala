package raytrace

import java.awt.Color

class IntersectData(t:Double,p:Point,n:Vect,c:Color,r:Double,g:Geometry) {
    val time=t
    val point=p
    val norm=n.normalize
    val color=c
    val reflect=r
    val geom=g
    
    def this(t:Double,p:Point,v:Vect,g:Geometry) = this(t,p,v,Color.white,0,g)
}
