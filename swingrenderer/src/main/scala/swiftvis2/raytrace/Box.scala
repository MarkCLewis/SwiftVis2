package swiftvis2.raytrace

trait Box {
  def min: Point
  def max: Point

  def movedBy(v: Vect): Box

  def intersectParam(r: Ray): Option[(Double, Vect, Double, Vect)] = {
    ???
  }

}