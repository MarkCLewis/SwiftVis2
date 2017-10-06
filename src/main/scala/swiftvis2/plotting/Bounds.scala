package swiftvis2.plotting

/**
 * This class is used to represent rectangular bounds. In some cases it is full pixel
 * size bounds for rending. In many cases it is used with values between 0.0 and 1.0
 * to represent a fractional bounds.
 */
case class Bounds(x: Double, y: Double, width: Double, height: Double) {
  /**
   * Calculates the center of the bounds in X.
   */
  def centerX = x+width/2
  
  /**
   * Calculates the center of the bounds in Y.
   */
  def centerY = y+height/2
  
  /**
   * Produces a new Bounds object with range in X determined by the fractional min and max arguments. Passing 0.0 and 1.0 would
   * reproduce the original object.
   * @param min The fractional relative minimum bound.
   * @param max The fractional relative maximum bound.
   */
  def subX(min: Double, max: Double): Bounds = {
    copy(x = x+min*width, width = (max-min)*width)
  }
  
  /**
   * Produces a new Bounds object with range in Y determined by the fractional min and max arguments. Passing 0.0 and 1.0 would
   * reproduce the original object.
   * @param min The fractional relative minimum bound.
   * @param max The fractional relative maximum bound.
   */
  def subY(min: Double, max: Double): Bounds = {
    copy(y = y+min*height, height = (max-min)*height)
  }
  
  /**
   * Produces a new Bounds object with range in X and Y determined by the fractional min and max arguments. Passing 0.0, 1.0, 0.0, 1.0 would
   * reproduce the original object.
   * @param minx The fractional relative minimum bound for X.
   * @param maxx The fractional relative maximum bound for X.
   * @param miny The fractional relative minimum bound for Y.
   * @param maxy The fractional relative maximum bound for Y.
   */
  def subXY(minx: Double, maxx: Double, miny: Double, maxy: Double): Bounds = {
    Bounds(x+minx*width, y+miny*height, (maxx-minx)*width, (maxy-miny)*height) 
  }

  /**
   * Produces a new Bounds object with range in X and Y determined by the fractional bounds object.
   * @param bounds A fractional bounds used to make the sub-bounds.
   */
  def subXY(b: Bounds): Bounds = {
    Bounds(x+b.x*width, y+b.y*height, b.width*width, b.height*height) 
  }
  
  /**
   * Produces a new Bounds with absolute offsets for the X and Y values.
   * @param minxBorder The absolute width of the X offset on the minimum edge.
   * @param maxxBorder The absolute width of the X offset on the maximum edge.
   * @param minyBorder The absolute height of the Y offset on the minimum edge.
   * @param maxyBorder The absolute height of the Y offset on the maximum edge.
   */
  def subXYBorder(minxBorder: Double, maxxBorder: Double, minyBorder: Double, maxyBorder: Double): Bounds = {
    Bounds(x+minxBorder, y+minyBorder, width-(minxBorder+maxxBorder), height-(minyBorder+maxyBorder))
  }
}