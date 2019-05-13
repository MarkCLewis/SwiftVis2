package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.{BarStyle, HistogramStyle, ScatterStyle}

case class PlotLegend(bounds: Bounds,
                      colorNames: Map[Int, String] = Map.empty,
                      gradientNames: Map[ColorGradient, String] = Map.empty,
                      sizingName: (String, Seq[(Int, Int)]) = ("", Seq.empty),
                      symbolNames: Map[PlotSymbol, String] = Map.empty) extends Product with Serializable with Plottable {
  def lines: Seq[LegendItem] = {
    (colorNames.map(x => LegendItem(x._2, x._1)) ++ symbolNames.map(name => LegendItem(name._2, symbol = name._1)) ++
      Seq(LegendItem(sizingName._1, sizedPoints = sizingName._2))).toSeq.filter(_.hasContent)
  }
  def render(r: Renderer, rendBounds: Bounds): Unit = {
  	val fontSize = r.maxFontSize(lines.map(_.text), rendBounds.width - 35, rendBounds.height / lines.length, Renderer.FontData("Ariel", Renderer.FontStyle.Plain))
    r.drawRectangle(rendBounds)
    for(i <- lines.indices) {
      lines(i).render(r, Bounds(rendBounds.x, rendBounds.y + (rendBounds.height / lines.length * i), rendBounds.width, rendBounds.height / lines.length), fontSize)
    }
  }
}

object PlotLegend {
  def legend(grid: PlotGrid, colorNames: Seq[String], symbolNames: Seq[String] = Seq.empty): PlotLegend = {
    val styles = for(row <- grid.plots; col <- row; plot <- col) yield plot
    val distinctColors = styles.flatMap(plot => plot.style match {
      case b: BarStyle => b.valSourceColor.map(x => x.color).distinct
      case h: HistogramStyle => h.valSourceColor.map(x => x.color).distinct
      case s: ScatterStyle => Range(s.xDataMin().get.toInt, s.xDataMax().get.toInt).map(s.colors).distinct
      case _ => Seq.empty
    }).distinct
    val distinctSymbols = styles.flatMap(plot => plot.style match {
      case s: ScatterStyle => Seq(s.symbol)
      case _ => Seq.empty
    }).distinct
    PlotLegend(Bounds(.75, .75, .1, .1),
      (for(i <- colorNames.indices) yield distinctColors(i) -> colorNames(i)).toMap,
      symbolNames = (for(i <- symbolNames.indices) yield distinctSymbols(i) -> colorNames(i)).toMap)
  }
}