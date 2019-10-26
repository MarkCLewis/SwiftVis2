package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

/**
 * Abstract supertype for axes.
 */
sealed trait Axis {
  val displaySide: Axis.DisplaySide.Value
  val name: Option[Axis.NameSettings]
  def isDrawn: Boolean
}

/**
 * An axis type that displays a numeric scaling. The values are auto-scaled when values for the minimum or maximum aren't provided.
 * If no tick spacing is provided, it will pick a "nice" spacing that gives 5-6 ticks in the range displayed. Axes can be set to
 * display on the minimum or maximum side of a plot, and they can be linear or logarithmic. Note that when you use a logarithmic scale,
 * the tickSpacing will be ignored, even if it is provided.
 */
case class NumericAxis(
  key:           String,
  min:           Option[Double]                      = None,
  max:           Option[Double]                      = None,
  tickSpacing:   Option[Double]                      = None,
  tickStyle:     Axis.TickStyle.Value                = Axis.TickStyle.Both,
  tickLabelInfo: Option[Axis.LabelSettings]          = None, // Angle in degrees, None if no labels shown
  name:          Option[Axis.NameSettings]           = None,
  displaySide:   Axis.DisplaySide.Value              = Axis.DisplaySide.Min,
  style:         Axis.ScaleStyle.Value               = Axis.ScaleStyle.Linear) extends Axis {

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
    (toPixels, tickSize, nameSize, (tfs, nfs, aggBounds, nextAxis) => render(r, tickBounds, tfs, nameBounds, aggBounds, nextAxis, nfs, orient, tickLocs, toPixels))
  }

  def toPixelFunc(pmin: Double, pmax: Double, amin: Double, amax: Double): Double => Double = {
    style match {
      case Axis.ScaleStyle.Linear =>
        val scale = (pmax - pmin) / (amax - amin)
        x => pmin + (x - amin) * scale
      case Axis.ScaleStyle.LogDense | Axis.ScaleStyle.LogSparse =>
        val scale = (pmax - pmin) / (math.log10(amax / amin))
        val logmin = math.log10(amin)
        x => pmin + (math.log10(x) - logmin) * scale
    }
  }

  def fromPixelFunc(pmin: Double, pmax: Double, dataMin: => Double, dataMax: => Double): Double => Double = {
    val amin = min.getOrElse(dataMin)
    val amax = max.getOrElse(dataMax)
    style match {
      case Axis.ScaleStyle.Linear =>
        val scale = (amax - amin) / (pmax - pmin)
        p => amin + (p - pmin) * scale
      case Axis.ScaleStyle.LogDense | Axis.ScaleStyle.LogSparse =>
        val scale = (math.log10(amax / amin)) / (pmax - pmin)
        val logmin = math.log10(amin)
        p => math.pow(10, (p - pmin) * scale + logmin)
    }
  }
  
  // Fluent Interface
  
  def asMinSideXAxis: NumericAxis = copy(tickLabelInfo = tickLabelInfo.map(_.copy(angle = 90)), displaySide = Axis.DisplaySide.Min)
  def asMaxSideXAxis: NumericAxis = copy(tickLabelInfo = tickLabelInfo.map(_.copy(angle = -90)), displaySide = Axis.DisplaySide.Max)
  def asMinSideYAxis: NumericAxis = copy(tickLabelInfo = tickLabelInfo.map(_.copy(angle = 90)), displaySide = Axis.DisplaySide.Min)
  def asMaxSideYAxis: NumericAxis = copy(tickLabelInfo = tickLabelInfo.map(_.copy(angle = 90)), displaySide = Axis.DisplaySide.Max)
  
  def updatedScaleStyle(newStyle: Axis.ScaleStyle.Value): NumericAxis = copy(style = newStyle) 
  def updatedScaleStyle(f: Axis.ScaleStyle.Value => Axis.ScaleStyle.Value): NumericAxis = copy(style = f(style))
  
  def updatedName(newName: String): NumericAxis = copy(name = name.map(_.copy(name = newName)))
  // TODO - More matching methods go here.
  
  // TODO - More fluent interface here.
  
  // Private methods

  private def boundsAndSizing(r: Renderer, bounds: Bounds, orient: Axis.RenderOrientation.Value, amin: Double, amax: Double): (Bounds, Double, Bounds, Double, Seq[Double]) = {
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

  private def render(r: Renderer, tickBounds: Bounds, tickFontSize: Double, nameBounds: Bounds, aggBounds: Option[Bounds], nextAxis: Option[Axis], 
      nameFontSize: Double, orient: Axis.RenderOrientation.Value, tickLocs: Seq[Double], toPixels: Axis.UnitConverter): Option[Bounds] = {

    // Draw ticks and labels
    orient match {
      case Axis.RenderOrientation.XAxis =>
        tickLabelInfo.foreach(tli => r.setFont(tli.font, tickFontSize))
        val cy = tickBounds.y + (if (displaySide == Axis.DisplaySide.Min) 0.0 else tickBounds.height)
        val tickLen = tickBounds.height * 0.1
        val labelY = cy + tickLen * (if (displaySide == Axis.DisplaySide.Min) 1 else -1)
        for (x <- tickLocs) {
          val px = toPixels(x)
          Axis.TickStyle.drawTick(r, tickStyle, orient, px, cy, displaySide, tickLen)
          tickLabelInfo.foreach { tli =>
            val textAlign = if (tli.angle % 180.0 == 0) Renderer.HorizontalAlign.Center else Renderer.HorizontalAlign.Left
            r.drawText(tli.numberFormat.format(x), px, labelY, textAlign, tli.angle)
          }
        }
      case Axis.RenderOrientation.YAxis =>
        tickLabelInfo.foreach(tli => r.setFont(tli.font, tickFontSize))
        val cx = tickBounds.x + (if (displaySide == Axis.DisplaySide.Min) tickBounds.width else 0.0)
        val tickLen = tickBounds.width * 0.1
        val labelX = cx + tickLen * (if (displaySide == Axis.DisplaySide.Min) -1 else 1)
        for (y <- tickLocs) {
          val py = toPixels(y)
          Axis.TickStyle.drawTick(r, tickStyle, orient, cx, py, displaySide, tickLen)
          tickLabelInfo.foreach { tli =>
            val textAlign = if ((tli.angle + 90) % 180.0 == 0) Renderer.HorizontalAlign.Center else if (displaySide == Axis.DisplaySide.Min) Renderer.HorizontalAlign.Right else Renderer.HorizontalAlign.Left
            r.drawText(tli.numberFormat.format(y), labelX, py, textAlign, tli.angle)
          }
        }
    }

    if(name == nextAxis.flatMap(_.name)) {
      aggBounds.map(b => b join nameBounds).orElse(Some(nameBounds))
    } else {
      // Draw name
      name.foreach { case Axis.NameSettings(nameStr, fd) => Axis.drawName(nameStr, fd, nameFontSize, aggBounds.map(_ join nameBounds).getOrElse(nameBounds), orient, r) }
      None
    }
  }

  def calcTickLocations(amin: Double, amax: Double): Seq[Double] = {
    if (tickSpacing.nonEmpty || tickLabelInfo.nonEmpty) {
      style match {
        case Axis.ScaleStyle.Linear =>
          val majorSep = tickSpacing.getOrElse {
            val str = "%e".format((amax - amin) / 6)
            (str.take(str.indexOf('.') + 2).toDouble.round + str.drop(str.indexOf('e'))).toDouble // TODO - consider BigDecimal here if display gets unhappy
          }
          val firstTickApprox = (amin / majorSep).toInt * majorSep
          val firstTick = firstTickApprox + (if ((amax - amin).abs < (amax - firstTickApprox).abs) majorSep else 0)
          firstTick to amax by majorSep
        case Axis.ScaleStyle.LogDense =>
          var pos = Math.pow(10, Math.floor(Math.log10(Math.min(amin, amax))))
          var ret = List[Double]()
          while (pos <= (amin max amax)) {
            if (pos >= (amin min amax)) {
              ret ::= pos
            }
            for (i <- 2 to 8 by 2; if pos * i <= (amin max amax) && pos * i >= (amin min amax)) ret ::= pos * i
            pos *= 10
          }
          ret.reverse
        case Axis.ScaleStyle.LogSparse =>
          var pos = Math.pow(10, Math.floor(Math.log10(Math.min(amin, amax))))
          var ret = List[Double]()
          while (pos <= (amin max amax)) {
            if (pos >= (amin min amax)) {
              ret ::= pos
            }
            pos *= 10
          }
          ret.reverse
      }
    } else Seq.empty
  }
}

/**
 * Axis with text categories for labels instead of numeric values.
 */
case class CategoryAxis(
  key:           String,
  tickStyle:        Axis.TickStyle.Value,
  labelOrientation: Double, // angle in degrees
  labelFont:        Renderer.FontData,
  name:             Option[Axis.NameSettings],
  displaySide:      Axis.DisplaySide.Value) extends Axis {

  def isDrawn: Boolean = true

  def renderInfo(categories: Seq[String], orient: Axis.RenderOrientation.Value,
                 r: Renderer, bounds: Seq[Bounds]): (Axis.CategoryLoc, Double, Double, Axis.AxisRenderer) = {
    val whichBounds = (if (orient == Axis.RenderOrientation.YAxis) 2 else 0) + (if (displaySide == Axis.DisplaySide.Max) 1 else 0)
    val (catBounds, catSize, nameBounds, nameSize) = boundsAndSizing(r, bounds(whichBounds), orient, categories)
    val fracWidth = bounds(whichBounds).width / categories.length
    val fracHeight = bounds(whichBounds).height / categories.length
    val catLocs = categories.zipWithIndex.map {
      case (c, i) =>
        val range = orient match {
          case Axis.RenderOrientation.XAxis => (bounds(whichBounds).x + i * fracWidth, bounds(whichBounds).x + (i + 1) * fracWidth)
          case Axis.RenderOrientation.YAxis => (bounds(whichBounds).y + bounds(whichBounds).height - i * fracHeight, bounds(whichBounds).y + bounds(whichBounds).height - (i + 1) * fracHeight)
        }
        c -> range
    }.toMap
    (catLocs, catSize, nameSize, (tfs, nfs, aggBounds, nextAxis) => render(r, catBounds, tfs, nameBounds, aggBounds, nextAxis, nfs, orient, categories, catLocs))
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

  def render(r: Renderer, tickBounds: Bounds, tickFontSize: Double, nameBounds: Bounds, aggBounds: Option[Bounds], nextAxis: Option[Axis], 
      nameFontSize: Double, orient: Axis.RenderOrientation.Value, categories: Seq[String], catLocs: Axis.CategoryLoc): Option[Bounds] = {

    // Draw ticks and labels
    orient match {
      case Axis.RenderOrientation.XAxis =>
        r.setFont(labelFont, tickFontSize)
        val cy = tickBounds.y + (if (displaySide == Axis.DisplaySide.Min) 0.0 else tickBounds.height)
        val tickLen = tickBounds.height * 0.1
        val labelY = cy + tickLen * (if (displaySide == Axis.DisplaySide.Min) 1 else -1)
        for (cat <- categories) {
          val (sx, ex) = catLocs(cat)
          Axis.TickStyle.drawTick(r, tickStyle, orient, (sx + ex) / 2, cy, displaySide, tickLen)
          if (labelOrientation % 180.0 == 0) {
            r.drawText(cat, (sx + ex) / 2, tickBounds.centerY, Renderer.HorizontalAlign.Center, labelOrientation)
          } else {
            r.drawText(cat, (sx + ex) / 2, labelY, Renderer.HorizontalAlign.Left, labelOrientation)
          }
        }
      case Axis.RenderOrientation.YAxis =>
        r.setFont(labelFont, tickFontSize)
        val cx = tickBounds.x + (if (displaySide == Axis.DisplaySide.Min) tickBounds.width else 0.0)
        val tickLen = tickBounds.width * 0.1
        val labelX = cx + tickLen * (if (displaySide == Axis.DisplaySide.Min) -1 else 1)
        for (cat <- categories) {
          val (sy, ey) = catLocs(cat)
          Axis.TickStyle.drawTick(r, tickStyle, orient, cx, (sy + ey) / 2, displaySide, tickLen)
          // TODO
          val textAlign = if ((labelOrientation + 90) % 180.0 == 0) Renderer.HorizontalAlign.Center else Renderer.HorizontalAlign.Right
          r.drawText(cat, labelX, (sy + ey) / 2, textAlign, labelOrientation)
        }
    }
        
    if(name == nextAxis.flatMap(_.name)) {
      aggBounds.map(b => b join nameBounds).orElse(Some(nameBounds))
    } else {
      // Draw name
      name.foreach { case Axis.NameSettings(nameStr, fd) => Axis.drawName(nameStr, fd, nameFontSize, aggBounds.map(_ join nameBounds).getOrElse(nameBounds), orient, r) }
      None
    }

  }
}

/**
 * Types, enumerations, and classes used for the axes. Also contains code shared by the different types of axes.
 */
object Axis {
  type UnitConverter = Double => Double
  type CategoryLoc = String => (Double, Double)
  type FontSizer = (Renderer, Bounds, Axis.RenderOrientation.Value) => (Double, Double)
  type AxisRenderer = (Double, Double, Option[Bounds], Option[Axis]) => Option[Bounds]
  case class LabelSettings(angle: Double, font: Renderer.FontData, numberFormat: String)
  case class NameSettings(name: String, font: Renderer.FontData)

  object TickStyle extends Enumeration {
    val Inner, Outer, Both, Neither = Value

    def drawTick(r: Renderer, v: Value, orient: RenderOrientation.Value, cx: Double, cy: Double, side: DisplaySide.Value, tickLen: Double): Unit = {
      if (v != Neither) {
        val (dx, dy) = orient match {
          case RenderOrientation.XAxis => (0.0, tickLen * (if (side == DisplaySide.Min) -1 else 1))
          case RenderOrientation.YAxis => (tickLen * (if (side == DisplaySide.Min) 1 else -1), 0.0)
        }
        val (x1, y1) = if (v == Inner || v == Both) (cx + dx, cy + dy) else (cx, cy)
        val (x2, y2) = if (v == Outer || v == Both) (cx - dx, cy - dy) else (cx, cy)
        r.drawLine(x1, y1, x2, y2)
      }
    }
  }
  
  // TODO - broadly reconsider the use of enumerations. Instead, consider using algebraic data types. There are two motivations here.
  // The log values could be merged to a single option that takes a sequence of the density for the plot.
  // Lots of things are currently wrapped in options. This seems to be a bit verbose in the fluent interface. ADTs might be shorter. Have to play with that.

  object ScaleStyle extends Enumeration {
    val Linear, LogSparse, LogDense = Value
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

  private[plotting] def calcBounds(bounds: Bounds, orient: Axis.RenderOrientation.Value, showLabels: Boolean,
                                   name: Option[NameSettings], displaySide: Axis.DisplaySide.Value): (Bounds, Bounds) = {
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

  private[plotting] def calcNameFontSize(nameBounds: Bounds, orient: Axis.RenderOrientation.Value, r: Renderer, name: Option[NameSettings]): Double = {
    name.map {
      case NameSettings(str, fd) =>
        val (w, h) = orient match {
          case Axis.RenderOrientation.XAxis => (nameBounds.width, nameBounds.height)
          case Axis.RenderOrientation.YAxis => (nameBounds.height, nameBounds.width)
        }
        r.maxFontSize(Seq(str), w, h, fd)
    }.getOrElse(0.0)
  }

  private[plotting] def calcLabelFontSize(labels: Seq[String], angle: Double, font: Renderer.FontData, orient: Axis.RenderOrientation.Value,
                                          tickBounds: Bounds, r: Renderer): Double = {
    val tickNum = labels.length max 1
    val labelWidth = Axis.RenderOrientation.axisWidth(orient, tickBounds, tickNum)
    val labelHeight = Axis.RenderOrientation.axisHeight(orient, tickBounds, tickNum)
    val rangle = angle * math.Pi / 180
    val tickFontWidth = labelWidth / math.cos(rangle).abs min labelHeight / math.sin(rangle).abs
    val tickFontHeight = labelWidth / math.sin(rangle).abs min labelHeight / math.cos(rangle).abs
    r.maxFontSize(labels, tickFontWidth, tickFontHeight, font)
  }

  private[plotting] def drawName(nameStr: String, fd: Renderer.FontData, nameFontSize: Double, nameBounds: Bounds, orient: Axis.RenderOrientation.Value, r: Renderer): Unit = {
    r.setFont(fd, nameFontSize)
    val angle = orient match {
      case Axis.RenderOrientation.XAxis => 0
      case Axis.RenderOrientation.YAxis => -90
    }
    r.drawText(nameStr, nameBounds.centerX, nameBounds.centerY, Renderer.HorizontalAlign.Center, angle)
  }
}
