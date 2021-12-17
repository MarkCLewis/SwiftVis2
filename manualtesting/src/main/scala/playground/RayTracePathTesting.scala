package playground

import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.image.{ImageView, WritableImage}
import swiftvis2.raytrace.LinearViewPath._
import swiftvis2.raytrace._


class FXRTImage(img: WritableImage) extends RTImage {
  def width: Int = img.width().toInt
  def height: Int = img.height().toInt
  def setColor(x: Int, y: Int, color: RTColor): Unit = {
    img.pixelWriter.setArgb(x, y, color.toARGB)
  }
}

object RayTracePathTesting extends JFXApp3 {
  def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "Ray Trace Test"
      scene = new Scene(720, 480) {
        val img = new WritableImage(720, 480)
        content = List(new ImageView(img))

        val spheres = Array.fill(200)(GeomSphere(swiftvis2.raytrace.Point(math.random, math.random, math.random()), 0.01, p => RTColor.White, p => 0.0))
        val tree = new KDTreeGeometry(spheres)
        val path = LinearViewPath(List(StopPoint(View(swiftvis2.raytrace.Point(0, -5, 0), Vect(0, 1, 0), Vect(0, 0, 1)), 1),
                                      StopPoint(View(swiftvis2.raytrace.Point(0, 0, 0), Vect(0, 1, 0), Vect(0, 0, 1)), 1),
                                      StopPoint(View(swiftvis2.raytrace.Point(0, 0, 0), Vect(1, 0, 0), Vect(0, 0, 1)), 1),
                                      StopPoint(View(swiftvis2.raytrace.Point(0, 0.5, 0.5), Vect(1, 0, 0), Vect(0, 1, 0)), 1),
                                      StopPoint(View(swiftvis2.raytrace.Point(0.5, 0.5, 0.5), Vect(1, 0, 0), Vect(0, 1, 0)), 1),
                                      StopPoint(View(swiftvis2.raytrace.Point(-0.5, 0.5, 0.5), Vect(1, 0, 0), Vect(0, 1, 0)), 1)),
                                  List(5, 2, 2, 5, 5), SmoothEasing)
        val lights = List(AmbientLight(RTColor(0.1, 0.1, 0.1)), DirectionLight(RTColor(1.0, 0.2, 0.0), Vect(0, 0, -1)),PointLight(RTColor.White, swiftvis2.raytrace.Point(-1, 0.5, 0.5)))
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
}