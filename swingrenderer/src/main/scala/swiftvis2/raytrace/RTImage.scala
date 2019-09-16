package swiftvis2.raytrace

trait RTImage {
  def width: Int
  def height: Int
  def setColor(x: Int, y: Int, color: RTColor): Unit
}