package playground

import swiftvis2.plotting.fxrenderer.JVMSVGInterface

object SVGPlotTesting extends App {
  import PlotTesting._

  JVMSVGInterface(scatterGrid(), "scatterGrid.svg", pubDim, pubDim)
  JVMSVGInterface(longForm(), "plot.svg", 1200, 1000)
  val rowPlot = rowOfDists()
  JVMSVGInterface(rowPlot, "rowOfDists.svg", pubDim, 300)
  val ptPlot = pressureTempPlot
  JVMSVGInterface(ptPlot, "pressureTempPlot.svg", pubDim, pubDim)
  JVMSVGInterface(simpleFull(), "simpleFull.svg", pubDim, pubDim)

}
