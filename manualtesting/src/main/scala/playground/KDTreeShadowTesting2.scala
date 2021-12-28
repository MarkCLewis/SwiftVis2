package playground

import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO
import swiftvis2.raytrace.LinearViewPath._
import swiftvis2.raytrace._

import scala.swing.{Alignment, Label, MainFrame, Swing}
import scala.collection.immutable.ArraySeq

class RTBufferedImage(img: BufferedImage) extends RTImage {
  def width: Int = img.getWidth
  def height: Int = img.getHeight
  def setColor(x: Int, y: Int, color: RTColor): Unit = {
    img.setRGB(x, y, color.toARGB)
  }
}

object KDTreeShadowTesting2 extends App {
  val img = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB)
  val rtImg = new RTBufferedImage(img)

  val size = 1e-5
  val spheres = ArraySeq.fill(2000)(GeomSphere(swiftvis2.raytrace.Point(math.random() * size - size / 2, math.random() * size, util.Random.nextGaussian()), 1e-7, p => RTColor.White, p => 0.0))
  val tree = new KDTreeGeometry(spheres, 1)
  val path = LinearViewPath(
    List(
      StopPoint(View(swiftvis2.raytrace.Point(0, -5e-6, 0), Vect(0, 10e-6, 0).normalize, Vect(0, 0, 10e-6).normalize), 10),
      StopPoint(View(swiftvis2.raytrace.Point(0, -5e-6, 10e-6), Vect(0, 20e-6, -10e-6).normalize, Vect(0, 10e-6, 20e-6).normalize), 10)),
    List(30), SmoothEasing)
  val lights = List(PointLight(RTColor.White, swiftvis2.raytrace.Point(0.0, -1 * math.cos(10 * math.Pi / 180), 1 * math.sin(10 * math.Pi / 180))))
  val offsetTrees = for (dx <- -3 to 3; dy <- -3 to 3) yield OffsetGeometry(tree, Vect(dx * size, dy * size, 0.0))
  val topTree = new KDTreeGeometry(offsetTrees)
  val frame = new MainFrame {
    title = "Trace Frame"
    contents = new Label("", Swing.Icon(img), Alignment.Center)
  }
  frame.visible = true
  for ((view, i) <- path.atIntervals(1).zipWithIndex) {
    println(view)
    RayTrace.render(view, rtImg, topTree, lights, 10)
    frame.repaint()
    val istr = i.toString
    ImageIO.write(img, "PNG", new File(s"Frame.${"0" * (4 - istr.length) + istr}.png"))
  }

}