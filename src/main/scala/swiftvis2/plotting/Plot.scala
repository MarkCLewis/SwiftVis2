package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.styles.ScatterStyle

case class Plot(texts: Map[String, Plot.PlotTextData], grids: Map[String, Plot.PlotGridData]) {
  def render(r: Renderer, bounds: Bounds) = {
    r.setColor(0xffffffff)
    r.fillRectangle(bounds)
    grids.foreach { case (_, g) => g.grid.render(r, bounds.subXY(g.bounds)) }
    texts.foreach { case (_, t) => t.text.render(r, bounds.subXY(t.bounds)) }
  }
}

object Plot {
  case class PlotTextData(text: PlotText, bounds: Bounds)
  case class PlotGridData(grid: PlotGrid, bounds: Bounds)
  def scatterPlot(x: PlotDoubleSeries, y: PlotDoubleSeries, title: String, xLabel: String, yLabel: String): Plot = {
    val text = PlotText(title, 0xff000000, Renderer.FontData("Ariel", Renderer.FontStyle.Plain), Renderer.HorizontalAlign.Center, 0.0)
    val style = ScatterStyle(x, y, Ellipse, 10, 10, PlotSymbol.Sizing.Pixels, PlotSymbol.Sizing.Pixels, BlackARGB, None)
    val grid = PlotGrid.oneByOne(style)
    Plot(Map("Title" -> PlotTextData(text, Bounds(0, 0, 1.0, 0.1))), 
        Map("Main" -> PlotGridData(grid, Bounds(0, 0.1, 0.98, 0.9))))
  }
}