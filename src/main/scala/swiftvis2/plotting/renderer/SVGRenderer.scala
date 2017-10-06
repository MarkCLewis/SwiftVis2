package swiftvis2.plotting.renderer

import swiftvis2.plotting.Bounds
import java.io.PrintStream
import swiftvis2.plotting._
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import scala.collection.mutable.Stack

class SVGRenderer(ps: PrintStream) extends Renderer {
  private var color = "#000000"
  private var strokeWidth = "1"
  private val stack = Stack[(String, String)]()
  
  ps.println(s"""<svg xmlns="http://www.w3.org/2000/svg" version="1.1">""")
  
  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<ellipse cx="$cx" cy="$cy" rx="${width*0.5}" ry="${height*0.5}" stroke="$color" stroke-width="$strokeWidth" fill="none" />""")
  }
  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<rect x="$x" y="$y" width="$width" height="$height" stroke="$color" stroke-width="$strokeWidth"  fill="none" />""")
  }
  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {}
  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {}
  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<ellipse cx="$cx" cy="$cy" rx="${width*0.5}" ry="${height*0.5}" fill="$color" />""")
  }
  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<rect x="$x" y="$y" width="$width" height="$height" fill="$color" />""")
  }
  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {}
  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {}
  
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    ps.println(s"""<line x1="$x1" y1="$y1" x2="$x2" y2="$y2" stroke="$color" stroke-width="$strokeWidth" />""")
  }
  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {
    ps.println(s"""<polyline stroke="$color" stroke-width="$strokeWidth" fill="none" points="${(for((px, py) <- x.zip(y)) yield s"$px, $py").mkString(" ")}"/>""")
  }
  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    ps.println(s"""<text x="$x" y="$y">$s</text>""")
    // TODO - Add rotations, font, and size
  }
  
  def setColor(argb: Int): Unit = color = {
    val hs = (argb & 0xffffff).toHexString
    "#"+"0"*(6-hs.length)+hs
  }
  def setStroke(stroke: Renderer.StrokeData): Unit = {
    strokeWidth = stroke.width.toString
    // TODO - dashing
  }
  def setFont(fd: Renderer.FontData, size: Double): Unit = {}
  def setClip(bounds: Bounds): Unit = {}
  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    10
  }
  
  // Needed for clipping for JavaFX
  def save(): Unit = {
    stack.push(color -> strokeWidth)
  }
  def restore(): Unit = {
    val (c, sw) = stack.pop()
    color = c
    strokeWidth = sw
  }
  
  def finish(): Unit = ps.println("</svg>")
}

object SVGRenderer {
  def apply(plot: Plot, filename: String, width: Double, height: Double): Unit = {
    val ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)))
    val r = new SVGRenderer(ps)
    plot.render(r, Bounds(0, 0, width, height))
    r.finish()
    ps.close
  }
}