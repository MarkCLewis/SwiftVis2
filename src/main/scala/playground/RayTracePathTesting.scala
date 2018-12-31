package playground

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.image.WritableImage
import scalafx.scene.image.ImageView
import swiftvis2.raytrace.RTImage
import swiftvis2.raytrace.RTColor
import swiftvis2.raytrace.KDTreeGeometry
import swiftvis2.raytrace.GeomSphere
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.LinearViewPath
import swiftvis2.raytrace.LinearViewPath._
import swiftvis2.raytrace.Vect
import scalafx.animation.AnimationTimer
import swiftvis2.raytrace.RayTrace
import swiftvis2.raytrace.AmbientLight
import swiftvis2.raytrace.DirectionLight
import swiftvis2.raytrace.ListScene
import swiftvis2.raytrace.PointLight


class FXRTImage(img: WritableImage) extends RTImage {
  def width: Int = img.width().toInt
  def height: Int = img.height().toInt
  def setColor(x: Int, y: Int, color: RTColor): Unit = {
    img.pixelWriter.setArgb(x, y, color.toARGB)
  }
}

object RayTracePathTesting extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title = "Ray Trace Test"
    scene = new Scene(500, 500) {
      val img = new WritableImage(500, 500)
      content = List(new ImageView(img))

      val spheres = Array.fill(200)(GeomSphere(Point(math.random, math.random, math.random), 0.01, p => RTColor.White, p => 0.0))
      val tree = new KDTreeGeometry(spheres)
//      val tree = new ListScene(spheres:_*)
      val path = LinearViewPath(List(StopPoint(View(Point(0, -5, 0), Vect(0, 1, 0), Vect(0, 0, 1)), 1),
                                     StopPoint(View(Point(0, 0, 0), Vect(0, 1, 0), Vect(0, 0, 1)), 1),
                                     StopPoint(View(Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 0, 1)), 1),
                                     StopPoint(View(Point(0, 0.5, 0.5), Vect(1, 0, 0), Vect(0, 1, 0)), 1),
                                     StopPoint(View(Point(0.5, 0.5, 0.5), Vect(1, 0, 0), Vect(0, 1, 0)), 1),
                                     StopPoint(View(Point(-0.5, 0.5, 0.5), Vect(1, 0, 0), Vect(0, 1, 0)), 1)),
                                List(5, 2, 2, 5, 5), SmoothEasing)
      val lights = List(AmbientLight(RTColor(0.1, 0.1, 0.1)), DirectionLight(RTColor(0.7, 0.2, 0.0), Vect(0, 0, -1)),PointLight(RTColor.White, Point(-1, 0.5, 0.5)))
      var firstTime = 0L
      val timer = AnimationTimer { time =>
        if(firstTime == 0) firstTime = time
        val ctime = (time-firstTime)*1e-9
        val view = path(ctime)
        println(ctime, view)
        RayTrace.render(view, new FXRTImage(img), tree, lights, 1)
      }
      timer.start
    }
  }
}