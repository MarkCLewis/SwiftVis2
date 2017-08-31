package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

sealed trait Axis {
  val displaySide: Axis.DisplaySide.Value
  def isDrawn: Boolean
}

case class NumericAxis(
    min: Option[Double],
    max: Option[Double],
    tickSpacing: Option[Double],
    tickStyle: Axis.TickStyle.Value,
    tickLabelInfo: Option[Axis.LabelSettings], // Angle in degrees, None if no labels shown
    name: Option[(String, Renderer.FontData)],
    displaySide: Axis.DisplaySide.Value,
    style: Axis.ScaleStyle.Value) extends Axis {

  def isDrawn: Boolean = {
    tickStyle != Axis.TickStyle.Neither || name.nonEmpty
  }

  def renderInfo(pmin: Double, pmax: Double, dataMin: => Double, dataMax: => Double, orient: Axis.RenderOrientation.Value,
                 r: Renderer, bounds: Seq[Bounds]): (Axis.UnitConverter, Double, Double, Axis.AxisRenderer) = {
    val amin = min.getOrElse(dataMin)
    val amax = max.getOrElse(dataMax)
    val toPixels = toPixelFunc(pmin, pmax, amin, amax)
    val whichBounds = (if (orient == Axis.RenderOrientation.YAxis) 2 else 0) + (if (displaySide == Axis.DisplaySide.Max) 1 else 0)
    val (tickBounds, tickSize, nameBounds, nameSize, tickLocs) = boundsAndSizing(r, bounds(whichBounds), orient, amin, amax)
    (toPixels, tickSize, nameSize, (tfs, nfs) => render(r, tickBounds, tfs, nameBounds, nfs, orient, tickLocs, toPixels))
  }

  def toPixelFunc(pmin: Double, pmax: Double, amin: Double, amax: Double): Double => Double = {
    style match {
      case Axis.ScaleStyle.Linear =>
        x => pmin + (x - amin) * (pmax - pmin) / (amax - amin)
      case Axis.ScaleStyle.Log => x =>
        ???
    }
  }

  def fromPixelFunc(pmin: Double, pmax: Double, dataMin: => Double, dataMax: => Double): Double => Double = {
    val amin = min.getOrElse(dataMin)
    val amax = max.getOrElse(dataMax)
    style match {
      case Axis.ScaleStyle.Linear =>
        p => amin + (p - pmin) * (amax - amin) / (pmax - pmin)
      case Axis.ScaleStyle.Log => x => 
        ???
    }
  }

  def boundsAndSizing(r: Renderer, bounds: Bounds, orient: Axis.RenderOrientation.Value, amin: Double, amax: Double): (Bounds, Double, Bounds, Double, Seq[Double]) = {
    // Calc tick and name bounds
    val (tickBounds, nameBounds) = Axis.calcBounds(bounds, orient, tickLabelInfo.nonEmpty, name, displaySide)

    // Make sequence of major tick locations
    val tickLocs = calcTickLocations(amin, amax)

    // Calculate tick font size
    val tickFontSize = tickLabelInfo.map { tli =>
      Axis.calcLabelFontSize(tickLocs.map(x => tli.numberFormat.format(x)), tli.angle, tli.font, orient, tickBounds, r)
    } getOrElse 0.0

    // Calculate name font size
    val nameFontSize = Axis.calcNameFontSize(nameBounds, orient, r, name)

    (tickBounds, tickFontSize, nameBounds, nameFontSize, tickLocs)
  }

  def render(r: Renderer, tickBounds: Bounds, tickFontSize: Double, nameBounds: Bounds, nameFontSize: Double,
             orient: Axis.RenderOrientation.Value, tickLocs: Seq[Double], toPixels: Axis.UnitConverter): Unit = {

    // Draw name
    name.foreach { case (nameStr, fd) => Axis.drawName(nameStr, fd, nameFontSize, nameBounds, orient, r) }

    // Draw ticks and labels
    orient match {
      case Axis.RenderOrientation.XAxis =>
        tickLabelInfo.foreach( tli => r.setFont(tli.font, tickFontSize))
        val cy = tickBounds.y + (if (displaySide == Axis.DisplaySide.Min) 0.0 else tickBounds.height)
        val tickLen = tickBounds.height * 0.1
        val labelY = cy+tickLen*(if(displaySide == Axis.DisplaySide.Min) 1 else -1)
        for (x <- tickLocs) {
          val px = toPixels(x)
          Axis.TickStyle.drawTick(r, tickStyle, orient, px, cy, displaySide, tickLen)
          tickLabelInfo.foreach { tli =>
            // TODO
            val textAlign = if(tli.angle % 180.0 == 0) Renderer.HorizontalAlign.Center else Renderer.HorizontalAlign.Left
            r.drawText(tli.numberFormat.format(x), px, labelY, textAlign, tli.angle)
          }
        }
      case Axis.RenderOrientation.YAxis =>
        tickLabelInfo.foreach( tli => r.setFont(tli.font, tickFontSize))
        val cx = tickBounds.x + (if (displaySide == Axis.DisplaySide.Min) tickBounds.width else 0.0)
        val tickLen = tickBounds.width * 0.1
        val labelX = cx+tickLen*(if(displaySide == Axis.DisplaySide.Min) -1 else 1)
        for (y <- tickLocs) {
          val py = toPixels(y)
          Axis.TickStyle.drawTick(r, tickStyle, orient, cx, py, displaySide, tickLen)
          tickLabelInfo.foreach { tli =>
            // TODO
            val textAlign = if((tli.angle+90) % 180.0 == 0) Renderer.HorizontalAlign.Center else Renderer.HorizontalAlign.Right
            r.drawText(tli.numberFormat.format(y), labelX, py, textAlign, tli.angle)
          }
        }
    }
  }

  def calcTickLocations(amin: Double, amax: Double): Seq[Double] = {
    if (tickSpacing.nonEmpty || tickLabelInfo.nonEmpty) {
      val majorSep = tickSpacing.getOrElse {
        val str = "%e".format((amax - amin) / 6)
        (str.take(str.indexOf('.') + 2).toDouble.round + str.drop(str.indexOf('e'))).toDouble // TODO - consider BigDecimal here if display gets unhappy
      }
      val firstTickApprox = (amin / majorSep).toInt * majorSep
      val firstTick = firstTickApprox + (if ((amax - amin).abs < (amax - firstTickApprox).abs) majorSep else 0)
      firstTick to amax by majorSep
    } else Seq.empty
  }
}

case class CategoryAxis(
    tickStyle: Axis.TickStyle.Value,
    labelOrientation: Double, // angle in degrees
    labelFont: Renderer.FontData,
    name: Option[(String, Renderer.FontData)],
    displaySide: Axis.DisplaySide.Value) extends Axis {

  def isDrawn: Boolean = true

  def renderInfo(categories: Seq[String], orient: Axis.RenderOrientation.Value,
                 r: Renderer, bounds: Seq[Bounds]): (Axis.CategoryLoc, Double, Double, Axis.AxisRenderer) = {
    val whichBounds = (if (orient == Axis.RenderOrientation.YAxis) 2 else 0) + (if (displaySide == Axis.DisplaySide.Max) 1 else 0)
    val (catBounds, catSize, nameBounds, nameSize) = boundsAndSizing(r, bounds(whichBounds), orient, categories)
    val fracWidth = bounds(whichBounds).width/categories.length
    val fracHeight = bounds(whichBounds).height/categories.length
    val catLocs = categories.zipWithIndex.map { case (c, i) =>
      val range = orient match {
        case Axis.RenderOrientation.XAxis => (bounds(whichBounds).x+i*fracWidth, bounds(whichBounds).x+(i+1)*fracWidth)
        case Axis.RenderOrientation.YAxis => (bounds(whichBounds).y+bounds(whichBounds).height-i*fracHeight, bounds(whichBounds).y+bounds(whichBounds).height-(i+1)*fracHeight)
      }
      c -> range
    }.toMap
    (catLocs, catSize, nameSize, (tfs, nfs) => render(r, catBounds, tfs, nameBounds, nfs, orient, categories, catLocs))
  }

  def boundsAndSizing(r: Renderer, bounds: Bounds, orient: Axis.RenderOrientation.Value, categories: Seq[String]): (Bounds, Double, Bounds, Double) = {
    // Calc tick and name bounds
    val (labelBounds, nameBounds) = Axis.calcBounds(bounds, orient, true, name, displaySide)

    // Calculate tick font size
    val labelFontSize = Axis.calcLabelFontSize(categories, labelOrientation, labelFont, orient, labelBounds, r)

    // Calculate name font size
    val nameFontSize = Axis.calcNameFontSize(nameBounds, orient, r, name)

    (labelBounds, labelFontSize, nameBounds, nameFontSize)
  }

  def render(r: Renderer, tickBounds: Bounds, tickFontSize: Double, nameBounds: Bounds, nameFontSize: Double,
             orient: Axis.RenderOrientation.Value, categories: Seq[String], catLocs: Axis.CategoryLoc): Unit = {

    // Draw name
    name.foreach { case (nameStr, fd) => Axis.drawName(nameStr, fd, nameFontSize, nameBounds, orient, r) }

    // Draw ticks and labels
    orient match {
      case Axis.RenderOrientation.XAxis =>
        r.setFont(labelFont, tickFontSize)
        val cy = tickBounds.y + (if (displaySide == Axis.DisplaySide.Min) 0.0 else tickBounds.height)
        val tickLen = tickBounds.height * 0.1
        val labelY = cy+tickLen*(if(displaySide == Axis.DisplaySide.Min) 1 else -1)
        for (cat <- categories) {
          val (sx, ex) = catLocs(cat)
          Axis.TickStyle.drawTick(r, tickStyle, orient, (sx+ex)/2, cy, displaySide, tickLen)
          if(labelOrientation % 180.0 == 0) {
            r.drawText(cat, (sx+ex)/2, tickBounds.centerY, Renderer.HorizontalAlign.Center, labelOrientation)
          } else {
            r.drawText(cat, (sx+ex)/2, labelY, Renderer.HorizontalAlign.Left, labelOrientation)          
          }
        }
      case Axis.RenderOrientation.YAxis =>
        r.setFont(labelFont, tickFontSize)
        val cx = tickBounds.x + (if (displaySide == Axis.DisplaySide.Min) tickBounds.width else 0.0)
        val tickLen = tickBounds.width * 0.1
        val labelX = cx+tickLen*(if(displaySide == Axis.DisplaySide.Min) -1 else 1)
        for (cat <- categories) {
          val (sy, ey) = catLocs(cat)
          Axis.TickStyle.drawTick(r, tickStyle, orient, cx, (sy+ey)/2, displaySide, tickLen)
          // TODO
          val textAlign = if((labelOrientation+90) % 180.0 == 0) Renderer.HorizontalAlign.Center else Renderer.HorizontalAlign.Right
          r.drawText(cat, labelX, (sy+ey)/2, textAlign, labelOrientation)
        }
    }
  }
}

object Axis {
  type UnitConverter = Double => Double
  type CategoryLoc = String => (Double, Double)
  type FontSizer = (Renderer, Bounds, Axis.RenderOrientation.Value) => (Double, Double)
  type AxisRenderer = (Double, Double) => Unit
  case class LabelSettings(angle: Double, font: Renderer.FontData, numberFormat: String)

  object TickStyle extends Enumeration {
    val Inner, Outer, Both, Neither = Value

    def drawTick(r: Renderer, v: Value, orient: RenderOrientation.Value, cx: Double, cy: Double, side: DisplaySide.Value, tickLen: Double): Unit = {
      if (v != Neither) {
        val (dx, dy) = orient match {
          case RenderOrientation.XAxis => (0.0, tickLen*(if(side==DisplaySide.Min) -1 else 1))
          case RenderOrientation.YAxis => (tickLen*(if(side==DisplaySide.Min) 1 else -1), 0.0)
        }
        val (x1, y1) = if (v == Inner || v == Both) (cx + dx, cy + dy) else (cx, cy)
        val (x2, y2) = if (v == Outer || v == Both) (cx - dx, cy - dy) else (cx, cy)
        r.drawLine(x1, y1, x2, y2)
      }
    }
  }

  object ScaleStyle extends Enumeration {
    val Linear, Log = Value
  }

  object DisplaySide extends Enumeration {
    val Min, Max = Value
  }

  object RenderOrientation extends Enumeration {
    val XAxis, YAxis = Value

    def axisWidth(v: Value, b: Bounds, divisions: Int): Double = v match {
      case XAxis => b.width / divisions
      case YAxis => b.width
    }

    def axisHeight(v: Value, b: Bounds, divisions: Int): Double = v match {
      case XAxis => b.height
      case YAxis => b.height / divisions
    }
  }
  
  def calcBounds(bounds: Bounds, orient: Axis.RenderOrientation.Value, showLabels: Boolean, 
      name: Option[(String, Renderer.FontData)], displaySide: Axis.DisplaySide.Value): (Bounds, Bounds) = {
    val tickFrac = 0.7 * (if (showLabels) 1.0 else 0.0)
    val nameFrac = 0.3 * (if (name.nonEmpty) 1.0 else 0.0)
    val fracSum = tickFrac + nameFrac
    orient match {
      case Axis.RenderOrientation.XAxis =>
        displaySide match {
          case Axis.DisplaySide.Min =>
            (bounds.subY(0.0, tickFrac / fracSum), bounds.subY(tickFrac / fracSum, 1.0))
          case Axis.DisplaySide.Max =>
            (bounds.subY(nameFrac / fracSum, 1.0), bounds.subY(0.0, nameFrac / fracSum))
        }
      case Axis.RenderOrientation.YAxis =>
        displaySide match {
          case Axis.DisplaySide.Min =>
            (bounds.subX(nameFrac / fracSum, 1.0), bounds.subX(0.0, nameFrac / fracSum))
          case Axis.DisplaySide.Max =>
            (bounds.subX(0.0, tickFrac / fracSum), bounds.subX(tickFrac / fracSum, 1.0))
        }
    }
  }

  def calcNameFontSize(nameBounds: Bounds, orient: Axis.RenderOrientation.Value, r: Renderer, name: Option[(String, Renderer.FontData)]): Double = {
    name.map {
      case (str, fd) =>
        val (w, h) = orient match {
          case Axis.RenderOrientation.XAxis => (nameBounds.width, nameBounds.height)
          case Axis.RenderOrientation.YAxis => (nameBounds.height, nameBounds.width)
        }
        r.maxFontSize(Seq(str), w, h, fd)
    }.getOrElse(0.0)
  }

  def calcLabelFontSize(labels: Seq[String], angle: Double, font: Renderer.FontData, orient: Axis.RenderOrientation.Value, 
      tickBounds: Bounds, r: Renderer): Double = {
    val tickNum = labels.length max 1
    val labelWidth = Axis.RenderOrientation.axisWidth(orient, tickBounds, tickNum)
    val labelHeight = Axis.RenderOrientation.axisHeight(orient, tickBounds, tickNum)
    val rangle = angle * math.Pi / 180
    val tickFontWidth = labelWidth / math.cos(rangle) min labelHeight / math.sin(rangle)
    val tickFontHeight = labelWidth / math.sin(rangle) min labelHeight / math.cos(rangle)
    r.maxFontSize(labels, tickFontWidth, tickFontHeight, font)
  }

  def drawName(nameStr: String, fd: Renderer.FontData, nameFontSize: Double, nameBounds: Bounds, orient: Axis.RenderOrientation.Value, r: Renderer): Unit = {
    r.setFont(fd, nameFontSize)
    val angle = orient match {
      case Axis.RenderOrientation.XAxis => 0
      case Axis.RenderOrientation.YAxis => -90
    }
    r.drawText(nameStr, nameBounds.centerX, nameBounds.centerY, Renderer.HorizontalAlign.Center, angle)
  }
}
