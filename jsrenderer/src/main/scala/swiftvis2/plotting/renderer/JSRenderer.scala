package swiftvis2.plotting.renderer

import org.scalajs.dom.ext.Color
import swiftvis2.plotting.Bounds
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.CanvasRenderingContext2D
import swiftvis2.plotting.renderer.Renderer.FontStyle

import scala.scalajs.js

class JSRenderer(canvas: Canvas) extends Renderer {
  val ctx = canvas.getContext("2d").asInstanceOf[js.Dynamic]
  val ctx2D = ctx.asInstanceOf[CanvasRenderingContext2D]
  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    ctx.beginPath()
    ctx.ellipse(cx, cy, width / 2, height / 2, 0, 0, math.Pi * 2)
    ctx.stroke()
  }

  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    ctx2D.strokeRect(x, y, width, height)
  }

  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    ctx2D.beginPath()
    ctx2D.moveTo(xs.head, ys.head)
    for(x <- xs.tail; y <- ys.tail) {
      ctx2D.lineTo(x, y)
    }
    ctx2D.stroke()
  }

  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {
    ctx2D.beginPath()
    ctx2D.moveTo(pnts.head._1, pnts.head._2)
    for((x, y) <- pnts.tail) {
      ctx2D.lineTo(x, y)
    }
    ctx2D.stroke()
  }

  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    ctx.beginPath()
    ctx.ellipse(cx, cy, width / 2, height / 2, 0, 0, math.Pi * 2)
    ctx.fill()
  }

  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    ctx2D.fillRect(x, y, width, height)
  }

  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    ctx2D.beginPath()
    ctx2D.moveTo(xs.head, ys.head)
    for(x <- xs.tail; y <- ys.tail) {
      ctx2D.lineTo(x, y)
    }
    ctx2D.fill()
  }

  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {
    ctx2D.beginPath()
    ctx2D.moveTo(pnts.head._1, pnts.head._2)
    for((x, y) <- pnts.tail) {
      ctx2D.lineTo(x, y)
    }
    ctx2D.fill()
  }

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    ctx2D.beginPath()
    ctx2D.moveTo(x1, y1)
    ctx2D.lineTo(x2, y2)
    ctx2D.stroke()
  }

  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {
    ctx2D.beginPath()
    ctx2D.moveTo(x.head, y.head)
    for(xp <- x; yp <- y) {
      ctx2D.lineTo(xp, yp)
    }
    ctx2D.stroke()
  }

  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    ctx2D.save
    ctx2D.textBaseline = "middle"
    ctx2D.translate(x, y)
    ctx2D.rotate(angle * (math.Pi / 180))
    ctx2D.textAlign = align match {
      case Renderer.HorizontalAlign.Left => "left"
      case Renderer.HorizontalAlign.Center => "center"
      case Renderer.HorizontalAlign.Right => "right"
    }
    ctx2D.fillText(s, 0, 0)
    ctx2D.restore
  }

  def setColor(argb: Int): Unit = {
    val color = new Color((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff)
    ctx2D.fillStyle = color.toHex
    ctx2D.strokeStyle = color.toHex
  }

  def setStroke(stroke: Renderer.StrokeData): Unit = {
    ctx2D.lineWidth = stroke.width
//    val arr = new js.Array[Double](stroke.dashing.length)
//    for(x <- stroke.dashing.indices) arr(x) = stroke.dashing(x)
//    ctx2D.setLineDash(arr)
  }

  def setFont(fd: Renderer.FontData, size: Double): Unit = {
    val style = fd.style match {
      case FontStyle.Plain => ""
      case FontStyle.Bold => "bold "
      case FontStyle.Italic => "italic "
      case FontStyle.Both => "bold italic "
    }
    ctx2D.font = size.toString ++ "px " ++ style ++ fd.font
  }

  def setClip(bounds: Bounds): Unit = {
    ctx2D.beginPath()
    ctx2D.rect(bounds.x, bounds.y, bounds.width, bounds.height)
    ctx2D.clip()
  }

  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    val initFont = ctx2D.font
    ctx2D.font = "10px serif"
    var maxWidth = 0.0
    var maxHeight = 13 //Standard height for 10pt font.
    for (s <- strings) {
      val bounds = ctx2D.measureText(s)
      if (bounds.width > maxWidth) maxWidth = bounds.width
//      val height = (bounds.asInstanceOf[js.Dynamic].fontBoundingBoxAscent + bounds.asInstanceOf[js.Dynamic].fontBoundingBoxDescent).asInstanceOf[Double]
//      if (height > maxHeight) maxHeight = height
    }
    ctx2D.font = initFont
    10 * (allowedWidth / maxWidth min allowedHeight / maxHeight)
  }

  def save(): Unit = ctx2D.save()

  def restore(): Unit = ctx2D.restore()

  def finish(): Unit = {}
}
