package swiftvis2.plotting.renderer

import swiftvis2.plotting.Plot

trait Updater {
  def update(newPlot: Plot): Unit
}
  
