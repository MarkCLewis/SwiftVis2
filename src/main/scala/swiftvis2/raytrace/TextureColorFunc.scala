package swiftvis2.raytrace

import java.awt.Color
import java.awt.image.BufferedImage

class TextureColorFunc(bi: BufferedImage, tl: Point, r: Vect, d: Vect) extends (Point => RTColor) {
  val img = bi
  val topLeft = tl
  val right = r.normalize
  val down = d.normalize
  val rInvStep = img.getWidth / r.magnitude
  val dInvStep = img.getHeight / d.magnitude

  def apply(p: Point): RTColor = {
    val rpix = ((right dot (p - topLeft)) * rInvStep).toInt
    val dpix = ((down dot (p - topLeft)) * dInvStep).toInt
    if (rpix < 0 || rpix >= img.getWidth || dpix < 0 || dpix >= img.getHeight) RTColor(0, 0, 0, 0)
    else RTColor(new Color(img.getRGB(rpix, dpix), true))
  }
}
