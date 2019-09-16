package playground

import swiftvis2.plotting.swingrenderer.SwingRenderer

object SwingPlotTesting extends App {
  import PlotTesting._

//  SwingRenderer(performanceTest(), largeDim, largeDim, true)
//  SwingRenderer(scatter1(), medDim, medDim)
//  SwingRenderer(scatter2(), medDim, medDim)
//  SwingRenderer(scatterLines(), medDim, medDim)
  // SwingRenderer(scatterGrid(), medDim, medDim)
  // SwingRenderer(scatterWithErrorBars(), medDim, medDim)
  // SwingRenderer(scatterMultidata(), medDim, medDim)
  SwingRenderer(scatterWithSizeandColor(), medDim, medDim)
  // SwingRenderer(scatterLogLog(), medDim, medDim)
//  SwingRenderer(fullScatter(), medDim, medDim)
//  SwingRenderer(stackedNNTest(), medDim, medDim)
//  SwingRenderer(gridNNTest(), medDim, medDim)
//  SwingRenderer(stackedCNTest(), medDim, medDim)
//  SwingRenderer(gridCNTest(), medDim, medDim)
  // SwingRenderer(barChart(), smallDim, 500)
  // SwingRenderer(histogram(), smallDim, smallDim)
//  SwingRenderer(histogramFromData(), smallDim, smallDim, true)
  // SwingRenderer(histogramSide(), smallDim, smallDim)
  // SwingRenderer(histogram2(), smallDim, smallDim)
  // SwingRenderer(histogramGrid(), medDim, medDim)
   SwingRenderer(longForm(), 1200, 1000)
  // SwingRenderer(boxPlot(), smallDim, smallDim)
  // SwingRenderer(violinPlot(), smallDim, smallDim)
  val rowPlot = rowOfDists()
  SwingRenderer(rowPlot, 1200, smallDim)
  val ptPlot = pressureTempPlot
  SwingRenderer(ptPlot, smallDim, smallDim)
  // SwingRenderer(colorTest(), largeDim, largeDim)
  // SwingRenderer(simpleFull(), smallDim, smallDim)
}
