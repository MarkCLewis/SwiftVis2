package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.renderer.Renderer.{FontData, FontStyle}

case class PlotLegend(lines: Seq[LegendItem], onPlot: Boolean = true) extends Product with Serializable with Plottable {

  def render(renderer: Renderer, bounds: Bounds): Unit = {
    def getNumIllus(line: LegendItem): Int = line match {
      case LegendItem (_, illus, sub) => illus.length + sub.foldLeft(0)(_ + getNumIllus(_))
    }
    // Do not draw if legend is empty
    if(lines.nonEmpty) {
      // Draw frame if on plot
      if(onPlot) {
        renderer.setColor(WhiteARGB)
        renderer.fillRectangle(bounds)
        renderer.setColor(BlackARGB)
        renderer.drawRectangle(bounds)
      }

      // Get font size
      val fd = FontData("Ariel", FontStyle.Plain)
      val lineFontSizes = lines.map(line => renderer.maxFontSize(Seq(line.desc), bounds.width * 0.7, bounds.height, fd))
      val fontSize = Math.min(18, lineFontSizes.min)

      // The number of illustrations (needed for height calculations)
      val numIllus = lines.foldLeft(0)(_ + getNumIllus(_))

      // Need a recursive helper since the items "seq" is really a forest
      def aux(lineSeq: Seq[LegendItem], indentation: Int, prevDraws: Int, spacing: Double): Int = lineSeq match {
        case Seq() => prevDraws
        case line +: lns =>
          val lheight = bounds.height * (line.illustrations.length.toDouble / numIllus.toDouble) * 0.9
          val lBounds = Bounds(bounds.x + (bounds.width * (2 * indentation) / 20), bounds.y
            + spacing, bounds.width, lheight)
          // Draw descriptions
          val symbolX = lBounds.x + (lBounds.width * 0.7)
          renderer.setColor(BlackARGB)
          renderer.setFont(fd, fontSize)
          renderer.drawText(line.desc, lBounds.x, lBounds.y + lBounds.height * 0.5, Renderer.HorizontalAlign.Left, 0)

          // Draw symbols
          for (j <- line.illustrations.indices) {
            val illustration = line.illustrations(j)
            val symbolYActual = (lBounds.y + j.toDouble * (lBounds.height / line.illustrations.length.toDouble)) + (lBounds.height / line.illustrations.length.toDouble) * 0.5
            illustration.gradient match {
              case None =>
                renderer.setColor (illustration.color (illustration.color.minIndex) )
                illustration.symbol match {
                  case Some(symbol) => symbol.drawSymbol(symbolX, symbolYActual, illustration.width, illustration.height, renderer)
                  case None =>
                }
              case Some(cg) =>
                renderer.setColor(BlackARGB)
                renderer.drawRectangle (symbolX, lBounds.y, lBounds.width, lBounds.height)
                val (colMin, colMax) = cg.colorValues.maxBy(_._1)._1 -> cg.colorValues.minBy(_._1)._1
                (0 to 19).foreach { i =>
                  renderer.setColor(cg(colMin + ((colMax - colMin) / 20) * i))
                  renderer.fillRectangle(symbolX, lBounds.y + i * lBounds.height / 20, lBounds.width, lBounds.height / 20)
                }
                val ax = NumericAxis("legend" ++ line.desc, None, None, None, Axis.TickStyle.Both,
                  Some(Axis.LabelSettings(0.0, fd, "%1.1f")), None, Axis.DisplaySide.Min)
                renderer.setColor(BlackARGB)
                val (uc, ts, _, ar) = ax.renderInfo(lBounds.y, lBounds.y + lBounds.height, colMin, colMax, Axis.RenderOrientation.YAxis,
                renderer, Seq(null, null, Bounds(symbolX - 15, symbolYActual, 15, lBounds.height)))
                // 1.75 seems to look good in most cases. It is definitely a magic number that should be changed
                ar(ts * 1.75, 0, None, None)
            }
          }
          val space = lheight
          val prev = aux(line.subItems, indentation + 1, prevDraws + Math.max(line.illustrations.length, 1), spacing)
          aux(lns, indentation, prev, space + spacing)
      }

      // Render legend items
      renderer.setFont(fd, fontSize)
      aux(lines, 0, 0, 0)
    } else ()
  }

  def withItem(legendItem: LegendItem): PlotLegend = copy(lines = lines :+ legendItem)

  def withItems(items: Seq[LegendItem]): PlotLegend = copy(lines = lines ++ items)

  def withModifiedItems(f: LegendItem => LegendItem): PlotLegend = copy(lines = lines.map(f))
}