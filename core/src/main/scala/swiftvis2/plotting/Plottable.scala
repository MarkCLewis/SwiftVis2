package swiftvis2.plotting

import swiftvis2.plotting.renderer.Renderer

trait Plottable {
  def render(r: Renderer, bounds: Bounds)
}