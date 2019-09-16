package swiftvis2.plotting.renderer

import java.io.{BufferedOutputStream, ByteArrayOutputStream, FileOutputStream, PrintStream}

import swiftvis2.plotting.{Bounds, Plot}

class JVMSVGInterface(ps: PrintStream, width: Double, height: Double) extends Renderer {
  val rend = new SVGRenderer(width, height)

  ps.println(s"""<svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="$width" height="$height">""")

  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = ps.println(rend.drawEllipse(cx, cy, width, height))

  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = ps.println(rend.drawRectangle(x, y, width, height))

  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = ps.println(rend.drawPolygon(xs, ys))

  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = ps.println(rend.drawPolygon(pnts))

  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = ps.println(rend.fillEllipse(cx, cy, width, height))

  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = ps.println(rend.fillRectangle(x, y, width, height))

  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = ps.println(rend.fillPolygon(xs, ys))

  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = ps.println(rend.fillPolygon(pnts))

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = ps.println(rend.drawLine(x1, y1, x2, y2))

  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = ps.println(rend.drawLinePath(x, y))

  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = ps.println(rend.drawText(s, x, y, align, angle))

  def setColor(argb: Int): Unit = rend.setColor(argb)

  def setStroke(stroke: Renderer.StrokeData): Unit = rend.setStroke(stroke)

  def setFont(fd: Renderer.FontData, size: Double): Unit = rend.setFont(fd, size)

  def setClip(bounds: Bounds): Unit = rend.setClip(bounds)

  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    rend.maxFontSize(strings, allowedWidth, allowedHeight, fd)
  }

  def save(): Unit = rend.save()

  def restore(): Unit = rend.restore()

  def finish(): Unit = ps.println(rend.finish())
}

object JVMSVGInterface {
  /**
   * Convenience method to render a plot to a SVG file.
   */
  def apply(plot: Plot, filename: String, width: Double, height: Double): Unit = {
    val ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)))
    val r = new JVMSVGInterface(ps, width, height)
    plot.render(r: JVMSVGInterface, Bounds(0, 0, width, height))
    ps.close()
  }

  /**
   * Convenience method to render a plot to an SVG string.
   */
  def stringValue(plot: Plot, width: Double, height: Double): String = {
    val baos = new ByteArrayOutputStream
    val r = new JVMSVGInterface(new PrintStream(baos), width, height)
    plot.render(r, Bounds(0, 0, width, height))
    baos.toString()
  }
}