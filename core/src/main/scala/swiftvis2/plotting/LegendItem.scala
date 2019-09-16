package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

case class LegendItem(
                       text: String,
                       color: Int = 0,
                       gradient: ColorGradient = ColorGradient((-1, -1)),
                       sizedPoints: Seq[(Int, Int)] = Seq.empty,
                       symbol: PlotSymbol = NoSymbol
                     ) extends Product  with Serializable {
  def render(r: Renderer, bounds: Bounds, fontSize: Double): Unit = {
    r.setColor(BlackARGB)
    r.setFont(Renderer.FontData("Ariel", Renderer.FontStyle.Plain), fontSize)
    r.drawText(text, bounds.x, bounds.y + bounds.height*0.5, Renderer.HorizontalAlign.Left, 0)
    r.setColor(color)
    if(symbol != NoSymbol) symbol.drawSymbol(bounds.x + bounds.width - 25, bounds.y, 25, 25, r)
    else r.fillRectangle(bounds.x + bounds.width - 25, bounds.y + bounds.height*0.3, 25, bounds.height / 2.5)
  }
  def hasContent: Boolean = !text.isEmpty
}