package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

object PlotSymbol {
  object Sizing extends Enumeration {
    val Pixels, Fraction, Scaled = Value
  }
}

sealed trait PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer)
}

case object Ellipse extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer) = r.fillEllipse(cx, cy, width, height)
}

case object Rectangle extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer) = r.fillRectangleC(cx, cy, width, height)
}

case object Triangle extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer) = ???
}

case object Star extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer) = ???
}

case object NoSymbol extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer) = {}
}