package swiftvis2.plotting.renderer

import scalafx.Includes._
import swiftvis2.plotting.Bounds
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.awt.geom.Path2D
import java.awt.geom.Line2D
import java.awt.Color
import java.awt.Stroke
import java.awt.BasicStroke
import javax.swing.JFrame
import swiftvis2.plotting.Plot
import java.awt.Graphics
import java.awt.FontMetrics
import java.awt.Font
import scala.collection.mutable.ArrayStack
import java.awt.Paint
import java.awt.Shape
import javax.swing.JPanel

/**
 * A renderer for drawing to the desktop using Swing. In theory, JavaFX is the way to go for the future, but in practice
 * this renderer is often easier to use, faster, and produces better looking output.
 */
object SwingRenderer {
  /**
   * Options used by the Swing renderer for the settings stack.
   */
  case class Options(color: Paint, stroke: Stroke, font: Font, clip: Shape)

  def apply(plot: Plot, width: Double = 800, height: Double = 800, makeMain: Boolean = false): Unit = {
    val panel = new JPanel() {
      override def paint(gr: Graphics) {
        val renderer = new SwingRenderer(gr.asInstanceOf[Graphics2D], 10000)
        plot.render(renderer, Bounds(0, 0, getWidth(), getHeight()))
      }
    }
    val frame = new JFrame("Plot Testing")
    frame.add(panel)
    // TODO - Add a menubar with printing options.
    frame.setSize(width.toInt, height.toInt)
    if (makeMain) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setVisible(true)
  }
}

class SwingRenderer(g: Graphics2D, maxQueue: Int) extends Renderer {
  import collection.mutable

  private var queue = mutable.Queue[() => Unit]()

  import SwingRenderer.Options

  private val stack = ArrayStack[Options]()

  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    g.draw(new Ellipse2D.Double(cx - width / 2, cy - height / 2, width, height))
  }

  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    g.draw(new Rectangle2D.Double(x, y, width, height))
  }

  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    val path = new Path2D.Double()
    path.moveTo(xs(0), ys(0))
    for (i <- 1 until xs.length) path.lineTo(xs(i), ys(i))
    g.draw(path)
  }

  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {
    val path = new Path2D.Double()
    path.moveTo(pnts(0)._1, pnts(0)._2)
    for (i <- 1 until pnts.length) path.lineTo(pnts(i)._1, pnts(i)._2)
    g.draw(path)
  }

  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    g.fill(new Ellipse2D.Double(cx - width / 2, cy - height / 2, width, height))
  }

  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    g.fill(new Rectangle2D.Double(x, y, width, height))
  }

  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    val path = new Path2D.Double()
    path.moveTo(xs(0), ys(0))
    for (i <- 1 until xs.length) path.lineTo(xs(i), ys(i))
    g.fill(path)
  }

  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {
    val path = new Path2D.Double()
    path.moveTo(pnts(0)._1, pnts(0)._2)
    for (i <- 1 until pnts.length) path.lineTo(pnts(i)._1, pnts(i)._2)
    g.fill(path)
  }

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    g.draw(new Line2D.Double(x1, y1, x2, y2))
  }

  def drawLinePath(xs: Seq[Double], ys: Seq[Double]): Unit = {
    val path = new Path2D.Double()
    path.moveTo(xs(0), ys(0))
    for (i <- 1 until xs.length) path.lineTo(xs(i), ys(i))
    g.draw(path)
  }

  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    val frc = g.getFontRenderContext()
    val oldTrans = g.getTransform()
    val bounds = g.getFont().getStringBounds(s, frc)
    val rangle = angle / 180 * math.Pi
    val voff = bounds.getHeight() * 0.35
    val hoff = align match {
      case Renderer.HorizontalAlign.Left   => 0.0
      case Renderer.HorizontalAlign.Center => -bounds.getWidth() / 2
      case Renderer.HorizontalAlign.Right  => -bounds.getWidth()
    }
    g.translate(x + hoff * math.cos(rangle) - voff * math.sin(rangle), y + hoff * math.sin(rangle) + voff * math.cos(rangle))
    g.rotate(rangle)
    g.drawString(s, 0, 0)
    g.setTransform(oldTrans)
  }

  def save(): Unit = { //enqueue{() =>
    val newOpts = Options(g.getPaint(), g.getStroke(), g.getFont(), g.getClip())
    stack.push(newOpts)
  }

  def restore(): Unit = { //enqueue{() =>
    val opts = stack.pop()
    g.setPaint(opts.color)
    g.setStroke(opts.stroke)
    g.setFont(opts.font)
    g.setClip(opts.clip)
  }

  def setColor(argb: Int): Unit = {
    val color = new Color((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, (argb >> 24) & 0xff)
    g.setPaint(color)
  }

  def setStroke(stroke: Renderer.StrokeData): Unit = {
    g.setStroke(new BasicStroke(stroke.width.toFloat))
    // TODO - add dashing
    //      if (stroke.dashing.nonEmpty) {
    //        gc.delegate.setLineDashes(stroke.dashing: _*)
    //      }
  }

  def setFont(fd: Renderer.FontData, size: Double): Unit = {
    g.setFont(new Font(fd.font, Font.PLAIN, size.toInt))
    // TODO - styles not implemented
  }

  def setClip(bounds: Bounds): Unit = {
    g.clip(new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height))
  }

  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    val frc = g.getFontRenderContext()
    var maxWidth = 0.0
    var maxHeight = 0.0
    for (s <- strings; if s != null) {
      val bounds = g.getFont().getStringBounds(s, frc)
      if (bounds.getWidth > maxWidth) maxWidth = bounds.getWidth
      if (bounds.getHeight > maxHeight) maxHeight = bounds.getHeight
    }
    g.getFont().getSize() * (allowedWidth / maxWidth min allowedHeight / maxHeight)
  }

  def finish(): Unit = {
  }
}