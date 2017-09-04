package swiftvis2.plotting

class ColorGradient private (colorValues: Seq[(Double, Int)]) extends (Double => Int) {
  def apply(x: Double): Int = {
    if (x < colorValues.head._1) colorValues.head._2
    else {
      val greaterIndex = colorValues.indices.find(i => colorValues(i)._1 > x)
      greaterIndex.map { i =>
        val x1 = colorValues(i - 1)._1
        val x2 = colorValues(i)._1
        val c1 = colorValues(i - 1)._2
        val c2 = colorValues(i)._2
        val (a1, r1, g1, b1) = (c1 >> 24, (c1 >> 16) & 0xff, (c1 >> 8) & 0xff, c1 & 0xff)
        val (a2, r2, g2, b2) = (c2 >> 24, (c2 >> 16) & 0xff, (c2 >> 8) & 0xff, c2 & 0xff)
        val f2 = (x - x1) / (x2 - x1)
        val f1 = 1.0 - f2
        ((a1*f1+a2*f2).toInt << 24) | ((r1*f1 + r2+f2).toInt << 16) | ((g1*f1 + g2*f2).toInt << 8) | (b1*f1 + b2*f2).toInt
      }.getOrElse(colorValues.last._2)
    }
  }
}

object ColorGradient {
  def apply(cv: (Double, Int)*): ColorGradient = {
    new ColorGradient(cv.sortBy(_._1))
  }
}