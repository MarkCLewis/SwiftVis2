package swiftvis2.raytrace

import java.awt.image.BufferedImage

case class SphereTextureColorFunc(img: BufferedImage, sphereCenter: Point) extends (Point => RTColor) {
  def apply(p: Point): RTColor = {
    val d = p - sphereCenter
    val theta = math.atan2(d.y, d.x)
    val phi = -math.atan2(d.z, math.sqrt(d.x*d.x + d.y*d.y))
    val xpix = ((theta + math.Pi)/(2*math.Pi)*img.getWidth).toInt max 0 min img.getWidth-1
    val ypix = ((phi + math.Pi/2)/math.Pi*img.getHeight).toInt max 0 min img.getHeight-1
    RTColor(img.getRGB(xpix, ypix))
  }
}