package swiftvis2.plotting.renderer

import swiftvis2.plotting.Bounds
import java.io.PrintStream

class SVGRenderer(ps: PrintStream) extends Renderer {
  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {}
  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {}
  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {}
  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {}
  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {}
  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {}
  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {}
  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {}
  
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {}
  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {}
  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {}
  
  def setColor(argb: Int): Unit = {}
  def setStroke(stroke: Renderer.StrokeData): Unit = {}
  def setFont(fd: Renderer.FontData, size: Double): Unit = {}
  def setClip(bounds: Bounds): Unit = {}
  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    10
  }
  
  // Needed for clipping for JavaFX
  def save(): Unit = {}
  def restore(): Unit = {}
}