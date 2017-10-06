package swiftvis2.plotting.renderer

import swiftvis2.plotting.Bounds
import java.io.PrintStream
import swiftvis2.plotting._
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import scala.collection.mutable.Stack

class SVGRenderer(ps: PrintStream) extends Renderer {
  import SVGRenderer.Options
  
  private var copt = Options("#000000", Renderer.StrokeData(1.0, Nil), Renderer.FontData("Ariel", Renderer.FontStyle.Plain), 10.0)
  private val stack = Stack[Options]()
  
  ps.println(s"""<svg xmlns="http://www.w3.org/2000/svg" version="1.1">""")
  
  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<ellipse cx="$cx" cy="$cy" rx="${width*0.5}" ry="${height*0.5}" stroke="${copt.color}" stroke-width="$strokeWidth" fill="none" />""")
  }
  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<rect x="$x" y="$y" width="$width" height="$height" stroke="${copt.color}" stroke-width="$strokeWidth"  fill="none" />""")
  }
  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {}
  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {}
  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<ellipse cx="$cx" cy="$cy" rx="${width*0.5}" ry="${height*0.5}" fill="${copt.color}" />""")
  }
  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    ps.println(s"""<rect x="$x" y="$y" width="$width" height="$height" fill="${copt.color}" />""")
  }
  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {}
  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {}
  
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    ps.println(s"""<line x1="$x1" y1="$y1" x2="$x2" y2="$y2" stroke="${copt.color}" stroke-width="$strokeWidth" />""")
    // TODO - dashing
  }
  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {
    ps.println(s"""<polyline stroke="${copt.color}" stroke-width="$strokeWidth" fill="none" points="${(for((px, py) <- x.zip(y)) yield s"$px, $py").mkString(" ")}"/>""")
    // TODO - dashing
  }
  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    val (dx, dy, anchor) = align match {
      case Renderer.HorizontalAlign.Left => 
        (0, fontSize/3, "start")
      case Renderer.HorizontalAlign.Center => 
        (0, fontSize/3, "middle")
      case Renderer.HorizontalAlign.Right => 
        (-0.06*s.length*fontSize, fontSize/3, "end")
    }

    ps.println(s"""<text x="$dx" y="$dy" font-family="$fontFamily" font-size="${fontSize}px" text-anchor="$anchor" transform="translate($x, $y) rotate($angle)">$s</text>""")
  }
  
  def setColor(argb: Int): Unit = copt = copt.copy(color = {
    val hs = (argb & 0xffffff).toHexString
    "#"+"0"*(6-hs.length)+hs
  })
  def setStroke(stroke: Renderer.StrokeData): Unit = {
    copt = copt.copy(stroke = stroke)
  }
  def setFont(fd: Renderer.FontData, size: Double): Unit = {
    copt = copt.copy(font = fd, fsize = size)
  }
  def setClip(bounds: Bounds): Unit = {
    // TODO
  }
  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    allowedHeight min 2*allowedWidth/strings.foldLeft(0.0)((m, s) => m max s.length)
  }
  
  // Needed for clipping for JavaFX
  def save(): Unit = {
    stack.push(copt)
  }
  def restore(): Unit = {
    copt = stack.pop()
  }
  
  def finish(): Unit = ps.println("</svg>")
  
  private def strokeWidth = copt.stroke.width
  private def fontSize = copt.fsize
  private def fontFamily = copt.font.font
  private def fontStyle = copt.font.style
}

object SVGRenderer {
  
  /**
   * Options used by the SVG renderer for the settings stack.
   */
  case class Options(color: String, stroke: Renderer.StrokeData, font: Renderer.FontData, fsize: Double)
  
  /**
   * Convenience method to render a plot to a SVG file.
   */
  def apply(plot: Plot, filename: String, width: Double, height: Double): Unit = {
    val ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)))
    val r = new SVGRenderer(ps)
    plot.render(r, Bounds(0, 0, width, height))
    r.finish()
    ps.close
  }
}