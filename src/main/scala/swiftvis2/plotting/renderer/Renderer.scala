package swiftvis2.plotting.renderer

import swiftvis2.plotting.Bounds

trait Renderer {
  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit
  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit
  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit
  def drawPolygon(pnts: Seq[(Double, Double)]): Unit
  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit
  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit
  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit
  def fillPolygon(pnts: Seq[(Double, Double)]): Unit
  
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit
  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit
  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit
  
  def setColor(argb: Int): Unit
  def setStroke(stroke: Renderer.StrokeData): Unit
  def setFont(fd: Renderer.FontData, size: Double): Unit
  def setClip(bounds: Bounds): Unit
  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double
  
  // Needed for clipping for JavaFX
  def save(): Unit
  def restore(): Unit

  def drawRectangle(bounds: Bounds): Unit = drawRectangle(bounds.x, bounds.y, bounds.width, bounds.height)
  def drawRectangleC(cx: Double, cy: Double, width: Double, height: Double): Unit = drawRectangle(cx-width/2, cy-height/2, width, height) 
  def drawRectangleP(x1: Double, y1: Double, x2: Double, y2: Double): Unit = drawRectangle(x1 min x2, y1 min y2, (x1-x2).abs, (y1-y2).abs) 
  def fillRectangle(bounds: Bounds): Unit = fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height)
  def fillRectangleC(cx: Double, cy: Double, width: Double, height: Double): Unit = fillRectangle(cx-width/2, cy-height/2, width, height)
  def fillRectangleP(x1: Double, y1: Double, x2: Double, y2: Double): Unit = fillRectangle(x1 min x2, y1 min y2, (x1-x2).abs, (y1-y2).abs)
}

object Renderer {
  object HorizontalAlign extends Enumeration {
    val Left, Center, Right = Value
  }
  
  object FontStyle extends Enumeration {
    val Plain, Bold, Italic, Both = Value
  }
  
  case class FontData(font: String, style: FontStyle.Value)
  
  case class StrokeData(width: Double, dashing: Seq[Double])
}