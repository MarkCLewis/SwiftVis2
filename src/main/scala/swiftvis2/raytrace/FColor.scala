package raytrace

import java.awt.Color

class FColor(red:Float,green:Float,blue:Float,alpha:Float) {
  val r=red
  val g=green
  val b=blue
  val a=alpha

  def this(c:Color) = this(c.getRed()/255.0f,c.getGreen()/255.0f,c.getBlue()/255.0f,c.getAlpha()/255.0f)      
  
  def +(fc:FColor) = new FColor(r+fc.r,g+fc.g,b+fc.b,a+fc.a)
  def *(fc:FColor) = new FColor(r*fc.r,g*fc.g,b*fc.b,a*fc.a)
  def *(v:Float) = new FColor(r*v,g*v,b*v,a*v)
  def /(v:Float) = new FColor(r/v,g/v,b/v,a/v)
  def toColor = {
    def range(f:Float) = if(f<0) 0f else if(f>1) 1f else f
    new Color(range(r),range(g),range(b),range(a))
  }
  override def toString = "FColor=("+r+","+g+","+b+","+a+")"
}
  
