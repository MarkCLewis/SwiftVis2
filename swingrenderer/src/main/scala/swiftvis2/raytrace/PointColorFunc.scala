package swiftvis2.raytrace

import java.awt.Color

class PointColorFunc(p:Array[Point], c:Array[Color]) {
    val pnts=p
    val cols=c
    
    def apply(p:Point) : Color = {
        val distances=pnts map ((x)=>1/x.distanceTo(p))
        val distSum=distances.foldLeft(0.0)(_+_)
        val weights=distances map (_/distSum)
        val drawRed=(cols zip weights).foldLeft(0.0) ((r,p)=>p match {case (c,w) => r+c.getRed()*w})
        val drawGreen=(cols zip weights).foldLeft(0.0) ((g,p)=>p match {case (c,w) => g+c.getGreen()*w})
        val drawBlue=(cols zip weights).foldLeft(0.0) ((b,p)=>p match {case (c,w) => b+c.getBlue()*w})
        val drawAlpha=(cols zip weights).foldLeft(0.0) ((a,p)=>p match {case (c,w) => a+c.getAlpha()*w})
        new Color(drawRed.toInt,drawGreen.toInt,drawBlue.toInt,drawAlpha.toInt)
    }
}
