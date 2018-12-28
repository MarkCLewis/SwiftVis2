package raytrace

import java.awt.Color

class PointColorFunc(p:Array[Point],c:Array[Color]) {
    val pnts=p
    val cols=c
    
    def apply(p:Point) : Color = {
        val distances=pnts map ((x)=>1/x.distanceTo(p))
        val distSum=(0.0 /: distances)(_+_)
        val weights=distances map (_/distSum)
        val drawRed=(0.0 /: (cols zip weights)) ((r,p)=>p match {case (c,w) => r+c.getRed()*w})
        val drawGreen=(0.0 /: (cols zip weights)) ((g,p)=>p match {case (c,w) => g+c.getGreen()*w})
        val drawBlue=(0.0 /: (cols zip weights)) ((b,p)=>p match {case (c,w) => b+c.getBlue()*w})
        val drawAlpha=(0.0 /: (cols zip weights)) ((a,p)=>p match {case (c,w) => a+c.getAlpha()*w})
        new Color(drawRed.toInt,drawGreen.toInt,drawBlue.toInt,drawAlpha.toInt)
    }
}
