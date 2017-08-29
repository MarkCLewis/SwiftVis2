package swiftvis2.plotting

case class Bounds(x: Double, y: Double, width: Double, height: Double) {
  def centerX = x+width/2
  def centerY = y+height/2
  
  def subX(min: Double, max: Double): Bounds = {
    copy(x = x+min*width, width = (max-min)*width)
  }
  
  def subY(min: Double, max: Double): Bounds = {
    copy(y = y+min*height, height = (max-min)*height)
  }
  
  def subXY(minx: Double, maxx: Double, miny: Double, maxy: Double): Bounds = {
    Bounds(x+minx*width, y+miny*height, (maxx-minx)*width, (maxy-miny)*height) 
  }

  def subXY(b: Bounds): Bounds = {
    Bounds(x+b.x*width, y+b.y*height, b.width*width, b.height*height) 
  }
  
  def subXYBorder(minxBorder: Double, maxxBorder: Double, minyBorder: Double, maxyBorder: Double): Bounds = {
    Bounds(x+minxBorder, y+minyBorder, width-(minxBorder+maxxBorder), height-(minyBorder+maxyBorder))
  }
}