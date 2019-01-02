package playground

import scalafx.application.JFXApp
import swiftvis2.raytrace.GeomSphere
import swiftvis2.raytrace.KDTreeGeometry
import swiftvis2.raytrace.DirectionLight
import swiftvis2.raytrace.LinearViewPath
import swiftvis2.raytrace.LinearViewPath._
import swiftvis2.raytrace.Vect
import swiftvis2.raytrace.RayTrace
import swiftvis2.raytrace.RTColor
import scalafx.scene.Scene
import swiftvis2.raytrace.Point
import swiftvis2.raytrace.PointLight
import scalafx.animation.AnimationTimer
import scalafx.scene.image.WritableImage
import swiftvis2.raytrace.AmbientLight
import scalafx.scene.image.ImageView
import swiftvis2.raytrace.SphereTextureColorFunc

object SphereTextureTest extends JFXApp {
  stage = new JFXApp.PrimaryStage {
    title = "Ray Trace Test"
    scene = new Scene(720, 480) {
      val img = new WritableImage(720, 480)
      content = List(new ImageView(img))

      val banish = javax.imageio.ImageIO.read(new java.net.URL("http://www.cs.trinity.edu/~mlewis/banish.gif"))
      val texture = SphereTextureColorFunc(banish, Point(0,0,0))
      val spheres = Array(
          GeomSphere(Point(0, 0, 0), 1, texture, p => 0.0),
          GeomSphere(Point(0.5, -2, 0), 0.5, p => RTColor.White, p => 0.0),
          GeomSphere(Point(0, 0, 2), 0.5, p => RTColor.White, p => 0.0))
      val tree = new KDTreeGeometry(spheres, 1)
      val view = View(Point(-1, -5, 0), Vect(1, 5, 0).normalize, Vect(0, 0, 1))
      val lights = List(
          AmbientLight(RTColor(0.1, 0.1, 0.1)), 
          DirectionLight(RTColor(1.0, 0.2, 0.0), Vect(0, 0, -1)), 
          PointLight(RTColor.White, Point(0.5, -10, 0.5)))
      RayTrace.render(view, new FXRTImage(img), tree, lights, 1)
    }
  }
}