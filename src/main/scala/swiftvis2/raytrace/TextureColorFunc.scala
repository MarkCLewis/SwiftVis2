package swiftvis2.raytrace

import java.awt.Color
import java.awt.image.BufferedImage

case class TextureColorFunc(img: BufferedImage, topLeft: Point, right: Vect, down: Vect) extends (Point => RTColor) {
  val rInvStep = img.getWidth / right.magnitude
  val dInvStep = img.getHeight / down.magnitude

  def apply(p: Point): RTColor = {
    val rpix = ((right dot (p - topLeft)) * rInvStep).toInt
    val dpix = ((down dot (p - topLeft)) * dInvStep).toInt
    if (rpix < 0 || rpix >= img.getWidth || dpix < 0 || dpix >= img.getHeight) RTColor(0, 0, 0, 0)
    else RTColor(img.getRGB(rpix, dpix))
  }
}
