package swiftvis2.plotting.styles

import swiftvis2.plotting.Bounds
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.Axis

trait PlotStyle {
  def render(r: Renderer, bounds: Bounds, xAxis: Axis, yAxis: Axis, axisBounds: Seq[Bounds]): (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer)
}