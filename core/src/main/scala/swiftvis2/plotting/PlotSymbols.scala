package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

object PlotSymbol {
  object Sizing extends Enumeration {
    val Pixels, Fraction, Scaled = Value
  }
  
  def sizing(sizeStyle: Sizing.Value, value: Double, size: Double, conv: Axis.UnitConverter, displaySize: Double): (Double, Double) = {
    sizeStyle match {
      case Sizing.Pixels => (conv(value)-size/2, conv(value)+size/2)
      case Sizing.Fraction => (conv(value)-size*displaySize/2, conv(value)+size*displaySize/2)
      case Sizing.Scaled =>
        val s2 = size/2
        val v1 = conv(value-s2)
        val v2 = conv(value+s2) 
        (v1 min v2, v2 max v1)
    }
  }
}

sealed trait PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit
}

case object Ellipse extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = r.fillEllipse(cx, cy, width, height)
}

case object EllipseLine extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = r.drawEllipse(cx, cy, width, height)
}

case object Rectangle extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = r.fillRectangleC(cx, cy, width, height)
}

case object RectangleLine extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = r.drawRectangleC(cx, cy, width, height)
}

case object Triangle extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = ???
}

case object Star extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = ???
}

case object NoSymbol extends PlotSymbol {
  def drawSymbol(cx: Double, cy: Double, width: Double, height: Double, r: Renderer): Unit = {}
}