package swiftvis2.raytrace

import scalafx.application.JFXApp

/**
 * This class represents a path for a point of view to move along for rendering. The first
 * argument is the points that the path hits with durations for how long it stops there.
 * The second is the durations to take between stops. It should be one shorter than the stops.
 */
case class LinearViewPath(stops: Seq[LinearViewPath.StopPoint], durations: Seq[Double], easing: Double => Double = LinearViewPath.LinearEasing) {
  require(stops.length - 1 == durations.length)
  import LinearViewPath._
  val zipped = stops.zip(durations :+ 0.0).toList
  val totalTime = stops.init.foldLeft(0.0)(_ + _.pause) + durations.sum // This excludes the last stops pause because we sit there at the end
  def apply(time: Double): View = {
    if (time <= 0.0) stops.head.view
    else if (time >= totalTime) stops.last.view
    else {
      def dropEarly(t: Double, lst: List[(StopPoint, Double)]): (List[(StopPoint, Double)], Double) = {
        val timeHere = lst.head._1.pause + lst.head._2
        if (t < timeHere) (lst, t) else dropEarly(t - timeHere, lst.tail)
      }
      val (at, when) = dropEarly(time, zipped)
      if (when > at.head._1.pause) {
        val frac = (when - at.head._1.pause) / at.head._2
        val loc1 = at.head._1.view.loc
        val dir1 = at.head._1.view.dir
        val up1 = at.head._1.view.up
        val loc2 = at(1)._1.view.loc
        val dir2 = at(1)._1.view.dir
        val up2 = at(1)._1.view.up
        View(loc1 + (loc2 - loc1) * frac, dir1 + (dir2 - dir1) * frac, up1 + (up2 - up1) * frac)
      } else at.head._1.view
    }
  }
  def atIntervals(dt: Double): Seq[View] = {
    def buildList(lst: List[(StopPoint, Double)], timeIn: Double): List[View] = lst match {
      case Nil      => Nil
      case h :: Nil => h._1.view :: (if (h._1.pause > timeIn) buildList(lst, timeIn + dt) else Nil)
      case h1 :: h2 :: t =>
        println(timeIn, h1, h2, t)
        if (h1._1.pause >= timeIn) h1._1.view :: buildList(lst, timeIn + dt)
        else if (h1._1.pause + h1._2 > timeIn) {
          val frac = (timeIn - h1._1.pause) / h1._2
          val loc1 = h1._1.view.loc
          val dir1 = h1._1.view.dir
          val up1 = h1._1.view.up
          val loc2 = h2._1.view.loc
          val dir2 = h2._1.view.dir
          val up2 = h2._1.view.up
          View(loc1 + (loc2 - loc1) * frac, dir1 + (dir2 - dir1) * frac, up1 + (up2 - up1) * frac) :: buildList(lst, timeIn + dt)
        } else buildList(h2 :: t, timeIn - (h1._1.pause + h1._2))
    }
    buildList(zipped, 0.0)
  }
}

object LinearViewPath {
  case class View(loc: Point, dir: Vect, up: Vect)
  case class StopPoint(view: View, pause: Double)

  val LinearEasing: Double => Double = x => x
  val SmoothEasing: Double => Double = x => (math.cos((x + math.Pi / 2) / math.Pi) + 1) / 2

}