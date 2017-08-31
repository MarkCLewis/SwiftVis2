package swiftvis2.plotting.styles

import swiftvis2.plotting.Bounds
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.Axis

trait PlotStyle {
  /**
   * This method will render the plot to the specified region using the provided renderer with the given axes and bounds for the axes.
   * @param r This is the renderer that plot will be drawn to.
   * @param bounds This is the region of the renderer that will draw to.
   * @param xAxis This is the x-axis for the style. Code should call renderInfo and pass through data.
   * @param yAxis This is the y-axis for the style. Code should call renderInfo and pass through data.
   * @param axisBounds These are passed in by the grid and used by the axes. You shouldn't use them for other purposes.
   * @return This is all information provided by the renderInfo methods of the axes that is returned to the grid.
   */
  def render(r: Renderer, bounds: Bounds, xAxis: Axis, yAxis: Axis, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer)
}