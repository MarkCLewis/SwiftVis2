package swiftvis2.plotting.renderer

import scalanative.native._

import sdl2.SDL._
import sdl2.Extras._
import sdl2.ttf.SDL_ttf._
import sdl2.ttf.Extras._
import swiftvis2.plotting.Bounds
import swiftvis2.plotting.Plot

class SDLRenderer(rend: Ptr[SDL_Renderer]) extends Renderer {
  private var fontSize = 25
  private var lineScale = 1.0
  def drawEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    val plt = pointsOnEllipse(cx, cy, width/2, height/2)
    plt.foreach { case (x, y) => plotEllipse(cx, cy, x, y)}
  }
  private def pointsOnEllipse(cx: Double, cy: Double, width: Double, height: Double): collection.mutable.Buffer[(Double, Double)] = {
    val res = collection.mutable.Buffer.empty[(Double, Double)]
    var x = 0.0
    var y = height
    var p = math.pow(height, 2) + (math.pow(width, 2) * (1 - 4 * height) - 2)/4
    var deltaE = 3 * math.pow(height, 2)
    var delta2E = math.pow(height, 2)
    var deltaSE = deltaE - math.pow(width, 2) * (height - 1)
    var delta2SE = delta2E + math.pow(width, 2)
    res.append((x, y))
    while(deltaSE < 2 * math.pow(width, 2) + 3 * math.pow(height, 2)) {
      if(p < 0) {
        p += deltaE
        deltaE += delta2E
        deltaSE += delta2E
      } else {
        p += deltaSE
        deltaE += delta2E
        deltaSE += delta2SE
        y -= 1
      }
      x += 1
      res.append((x, y))
    }
    p -= (math.pow(width, 2) * (4 * y - 3) + math.pow(height, 2) * (4 * x + 3) + 2)/4
    var deltaS = math.pow(width, 2) * (3 - 2 * y)
    deltaSE = 2 * math.pow(height, 2) + 3 * math.pow(width, 2)
    var delta2S = 2 * math.pow(width, 2)
    while(y > 0) {
      if(p > 0) {
        p += deltaS
        deltaS += delta2S
        deltaSE += delta2S
      } else {
        p += deltaSE
        deltaS += delta2S
        deltaSE += delta2SE
        x += 1
      }
      y -= 1
      res.append((x, y))
    }
    res
  }
  private def plotEllipse(cx: Double, cy: Double, x: Double, y: Double): Unit = {
    SDL_RenderDrawPoint(rend, (cx + x).toInt, (cy + y).toInt)
    SDL_RenderDrawPoint(rend, (cx + x).toInt, (cy - y).toInt)
    SDL_RenderDrawPoint(rend, (cx - x).toInt, (cy + y).toInt)
    SDL_RenderDrawPoint(rend, (cx - x).toInt, (cy - y).toInt)
  }
  private def plotFillEllipse(cx: Double, cy: Double, x: Double, y: Double): Unit = {
    drawLine(cx + x, cy + y, cx + x, cy - y)
    drawLine(cx - x, cy + y, cx - x, cy - y)
  }
  def drawRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    import sdl2.Extras.SDL_RectOps
    val rect = stackalloc[SDL_Rect]
    rect.init(x.toInt, y.toInt, width.toInt, height.toInt)
    SDL_RenderDrawRect(rend, rect)
  }
  def drawPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    val tuples = (for(i <- xs.indices) yield (xs(i), ys(i))).toSeq
    drawPolygon(tuples)
  }
  def drawPolygon(pnts: Seq[(Double, Double)]): Unit = {
    import sdl2.Extras.SDL_PointOps
    val arr = stackalloc[SDL_Point](pnts.length)
    for(i <- pnts.indices) (arr + i).init(pnts(i)._1.toInt, pnts(i)._2.toInt)
    SDL_RenderDrawLines(rend, arr, pnts.length - 1)
  }
  def fillEllipse(cx: Double, cy: Double, width: Double, height: Double): Unit = {
    val plt = pointsOnEllipse(cx, cy, width/2, height/2)
    plt.foreach { case (x, y) => plotFillEllipse(cx, cy, x, y)}
  }
  def fillRectangle(x: Double, y: Double, width: Double, height: Double): Unit = {
    val rect = stackalloc[SDL_Rect]
    rect.init(x.toInt, y.toInt, width.toInt, height.toInt)
    SDL_RenderFillRect(rend, rect)
  }
  def fillPolygon(xs: Seq[Double], ys: Seq[Double]): Unit = {
    ()
  }
  def fillPolygon(pnts: Seq[(Double, Double)]): Unit = {
    ()
  }
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    SDL_RenderDrawLine(rend, x1.toInt, y1.toInt, x2.toInt, y2.toInt)
  }
  def drawLinePath(x: Seq[Double], y: Seq[Double]): Unit = {
    import sdl2.Extras.SDL_PointOps
    val arr = stackalloc[SDL_Point](x.length)
    for(i <- x.indices) (arr + i).init(x(i).toInt, y(i).toInt)
    SDL_RenderDrawLines(rend, arr, x.length - 1)
  }
  def drawText(s: String, x: Double, y: Double, align: Renderer.HorizontalAlign.Value, angle: Double): Unit = {
    if(TTF_Init() == 1) {
      println(fromCString(c"TTF_Init error: {TTF_GetError}"))
    } else {
      val col: SDL_Color = ((0 << 24) + (0 << 16) + (0 << 8) + 0).toUInt
      val fontPtr = TTF_OpenFont(c"/home/nick/Programming/LewisResearch/SwiftVis2/nativerenderer/fonts/arial.ttf",
        fontSize)
      Zone { implicit z =>
        val buffer = alloc[Byte](s.length + 1)
        val txtSurf = TTF_RenderText_Solid(fontPtr, toCString(s), col)
        val txtTexture = SDL_CreateTextureFromSurface(rend, txtSurf)
        val destination = stackalloc[SDL_Rect]
        SDL_QueryTexture(txtTexture, null, null, destination._3, destination._4)
        val voff = -(destination.h * 0.5)
        val rangle = angle / 180 * Math.PI
        println(s)
        val hoff = align match {
            case Renderer.HorizontalAlign.Center => -(destination.w / 2)
            case Renderer.HorizontalAlign.Left => 0
            case Renderer.HorizontalAlign.Right => -destination.w
        }
        destination.x = (x + hoff * math.cos(rangle) + voff * math.sin(rangle)).toInt
        destination.y = (y + hoff * math.sin(rangle) + voff * math.cos(rangle)).toInt
        println(voff, hoff)
        println(destination.x, destination.y)
        //val center = stackalloc[SDL_Point]
        //renter.init(if(align == Renderer.HorizontalAlign.Left) (x - destination.w).toInt else if(align == Renderer.HorizontalAlign.Center) (x - destination.w / 2).toInt else x.toInt, (y - destination.h / 2).toInt)
        SDL_RenderCopyEx(rend, txtTexture, null, destination, angle, null, SDL_FLIP_NONE)
      }
      TTF_CloseFont(fontPtr)
    }
  }
  def setColor(argb: Int): Unit = {
    SDL_SetRenderDrawColor(rend, ((argb >> 16) & 0xff).toUByte, ((argb >> 8) & 0xff).toUByte, (argb & 0xff).toUByte, (((argb >> 24) & 0xff) / 255).toUByte)
  }
  def setStroke(stroke: Renderer.StrokeData): Unit = ()
  def setFont(fd: Renderer.FontData, size: Double): Unit = ()
  def setClip(bounds: Bounds): Unit = {
    val rect = stackalloc[SDL_Rect]
    rect.init(bounds.x.toInt, bounds.y.toInt, bounds.width.toInt, bounds.height.toInt)
    SDL_RenderSetClipRect(rend, rect)
  }
  def maxFontSize(strings: Seq[String], allowedWidth: Double, allowedHeight: Double, fd: Renderer.FontData): Double = 0

  // Needed for clipping for JavaFX
  def save(): Unit = ()
  def restore(): Unit = {
    val rect = stackalloc[SDL_Rect]
    SDL_RenderGetViewport(rend, rect)
    SDL_RenderSetClipRect(rend, rect)
  }

  // Called when a plot is fully rendered to close/flush things
  def finish(): Unit = {
    TTF_Quit()
  }
  def quit(): Unit = {
    SDL_DestroyRenderer(rend)
    SDL_Quit()
  }
}

object SDLRenderer {
  def apply(plot: Plot, pwidth: Double = 1000, pheight: Double = 1000): SDLRenderer = {
    SDL_Init(SDL_INIT_VIDEO)
    val window = SDL_CreateWindow(
      c"Test",
      SDL_WINDOWPOS_CENTERED,
      SDL_WINDOWPOS_CENTERED,
      1200,
      1000,
      SDL_WINDOW_RESIZABLE)
    val rend = SDL_CreateRenderer(window, -1, 0.toUInt)
    SDL_SetRenderDrawColor(rend, 255.toUByte, 255.toUByte, 255.toUByte, 255.toUByte)
    SDL_RenderClear(rend)
    SDL_SetRenderDrawColor(rend, 0.toUByte, 0.toUByte, 0.toUByte, 255.toUByte)
    val swiftRend = new SDLRenderer(rend)
    plot.render(swiftRend, Bounds(0,0,1200,1000))
    SDL_RenderPresent(rend)
    def loop(): Unit = {
      val event = stackalloc[SDL_Event]
      while (true) {
        while (SDL_PollEvent(event) != 0) {
          event.type_ match {
            case SDL_QUIT =>
              return
            case _ =>
              ()
          }
        }
      }
    }
    loop()
    SDL_DestroyWindow(window)
    swiftRend
  }
}
