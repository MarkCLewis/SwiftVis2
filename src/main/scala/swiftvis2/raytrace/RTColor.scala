package swiftvis2.raytrace

case class RTColor(r: Double, g: Double, b: Double, a: Double = 1.0) {
  def +(fc: RTColor) = new RTColor(r + fc.r, g + fc.g, b + fc.b, a + fc.a)
  def *(fc: RTColor) = new RTColor(r * fc.r, g * fc.g, b * fc.b, a * fc.a)
  def *(v: Double) = new RTColor(r * v, g * v, b * v, a * v)
  def /(v: Double) = new RTColor(r / v, g / v, b / v, a / v)
  private def range(f: Double): Float = if (f < 0) 0f else if (f > 1) 1f else f.toFloat
  def toAWTColor: java.awt.Color = {
    new java.awt.Color(range(r), range(g), range(b), range(a))
  }
  def toFXColor: scalafx.scene.paint.Color = {
    scalafx.scene.paint.Color(r min 0 max 1.0, g min 0 max 1.0, b min 0 max 1.0, a min 0 max 1.0)
  }
  def toARGB: Int = {
    (b * 255 min 255 max 0).toInt | ((g * 255 min 255 max 0).toInt << 8) | ((r * 255 min 255 max 0).toInt << 16) | ((a * 255 min 255 max 0).toInt << 24)
  }
  override def toString = "RTColor=(" + r + "," + g + "," + b + "," + a + ")"
}

object RTColor {
  def apply(c: java.awt.Color) = new RTColor(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0, c.getAlpha() / 255.0)
  def apply(c: scalafx.scene.paint.Color) = new RTColor(c.red / 255.0, c.green / 255.0, c.blue / 255.0, c.opacity / 255.0)
  def apply(argb: Int) = {
    val a = (argb >> 24) / 255.0
    val r = ((argb >> 16) & 0xFF) / 255.0
    val g = ((argb >> 8) & 0xFF) / 255.0
    val b = (argb & 0xFF) / 255.0
    new RTColor(r, g, b, a)
  }
  val White = RTColor(1, 1, 1)
  val Black = RTColor(0, 0, 0)
  val Red = RTColor(1, 0, 0)
  val Green = RTColor(0, 1, 0)
  val Blue = RTColor(0, 0, 1)
}
  
