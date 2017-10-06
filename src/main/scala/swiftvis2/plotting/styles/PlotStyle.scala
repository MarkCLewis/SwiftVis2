package swiftvis2.plotting.styles

import swiftvis2.plotting.Bounds
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.Axis

/**
 * This is the trait to be implemented by the various different styles of plots.
 */
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
  def render(r: Renderer, bounds: Bounds, xAxis: Axis, xminFunc: Axis => Double, xmaxFunc: Axis => Double, 
      yAxis: Axis, yminFunc: Axis => Double, ymaxFunc: Axis => Double, axisBounds: Seq[Bounds]): 
      (Seq[Double], Seq[Double], Axis.AxisRenderer, Axis.AxisRenderer)
      
  /**
   * Returns the minimum numeric value for the x-axis for this plot if applicable.
   */
  def xDataMin(): Option[Double]
  
  /**
   * Returns the maximum numeric value for the x-axis for this plot if applicable.
   */
  def xDataMax(): Option[Double]
    
  /**
   * Returns the minimum numeric value for the y-axis for this plot if applicable.
   */
  def yDataMin(): Option[Double]
  
  /**
   * Returns the maximum numeric value for the y-axis for this plot if applicable.
   */
  def yDataMax(): Option[Double]
}