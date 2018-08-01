package playground

import swiftvis2.plotting.renderer.SVGRenderer

object SVGPlotTesting extends App {
  import PlotTesting._

  SVGRenderer(scatterGrid(), "scatterGrid.svg", pubDim, pubDim)
  SVGRenderer(longForm(), "plot.svg", 1200, 1000)
  val rowPlot = rowOfDists()
  SVGRenderer(rowPlot, "rowOfDists.svg", pubDim, 300)
  val ptPlot = pressureTempPlot
  SVGRenderer(ptPlot, "pressureTempPlot.svg", pubDim, pubDim)
  SVGRenderer(simpleFull(), "simpleFull.svg", pubDim, pubDim)

}