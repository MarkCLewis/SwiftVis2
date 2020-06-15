package swiftvis2.plotting.renderer

import swiftvis2.plotting.Bounds
import scala.collection.mutable.ArrayStack
import slinky.web.svg._
import slinky.core.facade.ReactElement

class ReactRenderer(width: Double, height: Double) extends Renderer {
  val _elements = collection.mutable.Buffer.empty[ReactElement]
  val maker = new ReactRendererElementMaker

  def elements = _elements

  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = elements += maker.drawEllipse(cx, cy, width, height)

  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = elements += maker.drawRectangle(x, y, width, height)

  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = elements += maker.drawPolygon(xs, ys)

  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = elements += maker.drawPolygon(pnts)

  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = elements += maker.fillEllipse(cx, cy, width, height)

  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = elements += maker.fillRectangle(x, y, width, height)

  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = elements += maker.fillPolygon(xs, ys)

  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = elements += maker.fillPolygon(pnts)

  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = elements += maker.drawLine(x1, y1, x2, y2)

  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = elements += maker.drawLinePath(x, y)

  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = elements += maker.drawText(s, x, y, align, angle)

  def setColor(argb: Int): Unit = maker.setColor(argb)

  def setStroke(stroke: Renderer.StrokeData): Unit = maker.setStroke(stroke)

  def setFont(fd: Renderer.FontData, size: Double): Unit = maker.setFont(fd, size)

  def setClip(bounds: Bounds): Unit = maker.setClip(bounds)

  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    maker.maxFontSize(strings, allowedWidth, allowedHeight, fd)
  }

  def save(): Unit = maker.save()

  def restore(): Unit = maker.restore()

  def finish(): Unit = {}

}

class ReactRendererElementMaker {
  import SVGRenderer.Options

  private var copt = Options("#000000", Renderer.StrokeData(1.0, Nil), Renderer.FontData("Ariel", Renderer.FontStyle.Plain), 10.0, None)
  private val stack = ArrayStack[Options]()
  private var clipCnt = 0

  def drawEllipse(centerx: Double, centery: Double, width: Double, height: Double): ReactElement = {
    ellipse (cx:=centerx, cy:=centery, rx:=width * 0.5, ry:=height * 0.5, stroke:=s"${copt.color}", strokeWidth:=strokeWidthVal, fill:="none", clipPath:=clipPathOpt)
  }
  def drawRectangle(xpos: Double, ypos: Double, rwidth: Double, rheight: Double): ReactElement = {
    rect (x:=xpos, y:=ypos, width:=s"$rwidth", height:=s"$rheight", stroke:=copt.color, strokeWidth:=strokeWidthVal, fill:="none", clipPath:=clipPathOpt )
  }
  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): ReactElement = {???}
  def drawPolygon(pnts: Seq[(Double, Double)]): ReactElement = {???}
  def fillEllipse(centerx: Double, centery: Double, width: Double, height: Double): ReactElement = {
    ellipse (cx:=centerx, cy:=centery, rx:=width * 0.5, ry:=height * 0.5, fill:=copt.color, clipPath:=clipPathOpt)
  }
  def fillRectangle(xpos: Double, ypos: Double, rwidth: Double, rheight: Double): ReactElement = {
    rect (x:=xpos, y:=ypos, width:=s"$rwidth", height:=s"$rheight", stroke:=copt.color, fill:=copt.color, clipPath:=clipPathOpt )
  }
  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): ReactElement = {???}
  def fillPolygon(pnts: Seq[(Double, Double)]): ReactElement = {???}

  def drawLine(lx1: Double, ly1: Double, lx2: Double, ly2: Double): ReactElement = {
    line (x1:=lx1, y1:=ly1, x2:=lx2, y2:=ly2, stroke:=copt.color, strokeWidth:=strokeWidthVal, clipPath:=clipPathOpt)
    // TODO - dashing
  }
  def drawLinePath(x: Seq[Double], y: Seq[Double]): ReactElement = {
    polyline (stroke:=copt.color, strokeWidth:=strokeWidthVal, fill:="none", points:=s"${(for ((px, py) <- x.zip(y)) yield s"$px, $py").mkString(" ")}", clipPath:=clipPathOpt)
    // TODO - dashing
  }
  def drawText(s: String, px: Double, py: Double, align: Renderer.HorizontalAlign.Value, angle: Double): ReactElement = {
    val anchor = align match {
      case Renderer.HorizontalAlign.Left => "start"
      case Renderer.HorizontalAlign.Center => "middle"
      case Renderer.HorizontalAlign.Right => "end"
    }

    text (x:=0.0, y:=fontSizeVal / 3, fill:=copt.color, fontFamily:=fontFamilyVal, fontSize:=s"${fontSizeVal}px", textAnchor:=s"$anchor", transform:=s"translate($px, $py) rotate($angle)", clipPath:=clipPathOpt, s)
  }

  def setColor(argb: Int): Unit = copt = copt.copy(color = {
    val hs = (argb & 0xffffff).toHexString
    "#"+"0" * (6 - hs.length) + hs
  })
  def setStroke(stroke: Renderer.StrokeData): Unit = {
    copt = copt.copy(stroke = stroke)
  }
  def setFont(fd: Renderer.FontData, size: Double): Unit = {
    copt = copt.copy(font = fd, fsize = size)
  }
  def setClip(bounds: Bounds): ReactElement = {
    clipCnt += 1
    copt = copt.copy(withClip = Some(clipCnt))
    defs (
      clipPath ( id:=s"clip$clipCnt", rect (x:=bounds.x, y:=bounds.y, width:=bounds.width.toString, height:=bounds.height.toString))
    )
  }
  private def clipPathOpt = copt.withClip.map(n => s"""clip-path="url(#clip$n)" """)

  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = {
    allowedHeight min 2 * allowedWidth / strings.foldLeft(0.0)((m, s) => m max s.length)
  }

  // Needed for clipping for JavaFX
  def save(): Unit = {
    stack.push(copt)
  }
  def restore(): Unit = {
    copt = stack.pop()
  }

  private def strokeWidthVal = copt.stroke.width
  private def fontSizeVal = copt.fsize
  private def fontFamilyVal = copt.font.font
  private def fontStyleVal = copt.font.style
}