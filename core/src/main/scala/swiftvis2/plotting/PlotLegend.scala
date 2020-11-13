package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.renderer.Renderer.{FontData, FontStyle}

case class PlotLegend(lines: Seq[LegendItem], onPlot: Boolean = true) extends Product with Serializable with Plottable {

  def render(renderer: Renderer, bounds: Bounds): Unit = {
    if(onPlot) {
      renderer.setColor(WhiteARGB)
      renderer.fillRectangle(bounds)
      renderer.setColor(BlackARGB)
      renderer.drawRectangle(bounds)
    }
    def getNumIllus(line: LegendItem): Int = line match {
      case LegendItem (_, illus, sub) => illus.length + sub.foldLeft(0)(_ + getNumIllus(_))
    }
    // Need the number of illustrations so we can calculate the bounds for each item
    val numIllus = lines.map(getNumIllus).sum
    // The width and height of this bounds will apply to all items
    val lineBounds = Bounds(bounds.x + bounds.width / 8, bounds.y + bounds.height / 20, bounds.width * 7 / 8, bounds.height / numIllus)
    // The symbol should be centered at the same horizontal location for each line.
    val symbolX = lineBounds.x + lineBounds.width * 7 / 8

    // Get a font size that will work for all items
    val fd = FontData("Ariel", FontStyle.Plain)
    val lineFontSizes = lines.map(line => renderer.maxFontSize(Seq(line.desc), bounds.width * 0.7, bounds.height * 0.9, fd))
    val fontSize = Math.min(18, lineFontSizes.min)

    // The PlotLegend is a wrapper around a forest of LegendItems. This traverses that and draws the items.
    def traverseItemForest(forest: Seq[LegendItem], indentLevel: Int, prevDraws: Int): Int = forest match {
      case Seq() => prevDraws
      case line :: lns =>
        // Translate the base bounds by whatever y value we need to
        val lBounds = lineBounds.copy(y = lineBounds.y + prevDraws * lineBounds.height, x = lineBounds.x + lineBounds.x * 0.1 * indentLevel)
        // Draw descriptions
        val descBounds = lBounds.copy(width = lBounds.width / 2)
        renderer.setColor(BlackARGB)
        renderer.setFont(fd, fontSize)
        renderer.drawText(line.desc, descBounds.x, descBounds.y, Renderer.HorizontalAlign.Left, 0.0f)

        // Draw illustrations (splitting the line bounds up as needed if we have more than one illustration
        line.illustrations.indices.foreach { i =>
          val illustration = line.illustrations(i)
          // Check if we're drawing a gradient
          illustration.gradient match {
            // If not, just draw the symbol normally
            case None =>
              renderer.setColor(illustration.color)
              illustration.symbol match {
                case Some(symbol) => symbol.drawSymbol(symbolX, lBounds.y + lBounds.height * i, illustration.width, illustration.height, renderer)
                case None => ()
              }
            case Some(cg) =>
              renderer.setColor(BlackARGB)
              val gradientX = symbolX - lBounds.width * 0.15
              val gradientY = lBounds.y - lBounds.height / 2
              renderer.drawRectangle (gradientX, gradientY, illustration.width, lBounds.height)
              val (colMin, colMax) = cg.colorValues.maxBy(_._1)._1 -> cg.colorValues.minBy(_._1)._1
              (0 to 19).foreach { j =>
                renderer.setColor(cg(colMin + ((colMax - colMin) / 20) * j))
                renderer.fillRectangle(gradientX, gradientY + j * lBounds.height / 20, illustration.width, lBounds.height / 20)
              }
              val ax = NumericAxis(None, None, None, Axis.TickStyle.Both,
                Some(Axis.LabelSettings(0.0, fd, "%1.1f")), None, Axis.DisplaySide.Min)
              renderer.setColor(BlackARGB)
              val (uc, ts, _, ar) = ax.renderInfo(gradientY, gradientY + lBounds.height, colMin, colMax, Axis.RenderOrientation.YAxis,
                renderer, Seq(null, null, Bounds(gradientX - 15, lBounds.y, 15, lBounds.height)))
              // 1.75 seems to look good in most cases. It is definitely a magic number that should be changed
              ar(1.75 * ts, 0, None, None, false)
          }
        }
        val childRes = traverseItemForest(line.subItems, indentLevel + 1, prevDraws + line.illustrations.length)
        traverseItemForest(lns, indentLevel, prevDraws + (childRes - prevDraws))
    }
    traverseItemForest(lines, 0, 0)
  }

  def withItem(legendItem: LegendItem): PlotLegend = copy(lines = lines :+ legendItem)

  def withItems(items: Seq[LegendItem]): PlotLegend = copy(lines = lines ++ items)

  def withModifiedItems(f: LegendItem => LegendItem): PlotLegend = copy(lines = lines.map(f))
}