package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

case class PlotText(text: String, color: Int, font: Renderer.FontData, align: Renderer.HorizontalAlign.Value, angle: Double) extends Plottable {
  def render(r: Renderer, bounds: Bounds) = {
    val size = r.maxFontSize(Seq(text), bounds.width, bounds.height, font)
    r.setFont(font, size)
    r.setColor(color)
    align match {
      case Renderer.HorizontalAlign.Left =>
        r.drawText(text, bounds.x, bounds.y + bounds.height*0.5, align, angle)
      case Renderer.HorizontalAlign.Right =>
        r.drawText(text, bounds.x+bounds.width, bounds.y + bounds.height*0.5, align, angle)
      case Renderer.HorizontalAlign.Center =>
        r.drawText(text, bounds.x+bounds.width*0.5, bounds.y + bounds.height*0.5, align, angle)
    }
  }
}