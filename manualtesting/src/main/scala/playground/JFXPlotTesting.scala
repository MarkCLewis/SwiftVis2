package playground

import scalafx.application.JFXApp3
import swiftvis2.plotting.renderer.{FXRenderer, JVMSVGInterface}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object JFXPlotTesting extends JFXApp3 {
  def start(): Unit = {
    import PlotTesting._

    Future {
      FXRenderer(performanceTest(), largeDim, largeDim)
      FXRenderer(scatter1(), medDim, medDim)
      FXRenderer(scatter2(), medDim, medDim)
      FXRenderer(scatterLines(), medDim, medDim)
      FXRenderer(scatterGrid(), medDim, medDim)
      //JVMSVGInterface(scatSVGRendererGrid(), "scatterGrid.svg", pubDim, pubDim)
      FXRenderer(scatterWithErrorBars(), medDim, medDim)
      FXRenderer(scatterMultidata(), medDim, medDim)
      FXRenderer(scatterWithSizeandColor(), medDim, medDim)
      FXRenderer(scatterLogLog(), medDim, medDim)
      FXRenderer(fullScatter(), medDim, medDim)
      FXRenderer(stackedNNTest(), medDim, medDim)
      FXRenderer(gridNNTest(), medDim, medDim)
      FXRenderer(stackedCNTest(), medDim, medDim)
      FXRenderer(gridCNTest(), medDim, medDim)
      FXRenderer(barChart(), smallDim, 500)
      FXRenderer(histogram(), smallDim, smallDim)
      FXRenderer(histogramFromData(), smallDim, smallDim)
      FXRenderer(histogramSide(), smallDim, smallDim)
      FXRenderer(histogram2(), smallDim, smallDim)
      FXRenderer(histogramGrid(), medDim, medDim)
      FXRenderer(longForm(), 1200, 1000)
      JVMSVGInterface(longForm(), "plot.svg", 1200, 1000)
      FXRenderer(boxPlot(), smallDim, smallDim)
      FXRenderer(violinPlot(), smallDim, smallDim)
      val rowPlot = rowOfDists()
      FXRenderer(rowPlot, 1200, smallDim)
      val ptPlot = pressureTempPlot
      FXRenderer(ptPlot, smallDim, smallDim)
      FXRenderer(colorTest(), largeDim, largeDim)
      saveToFile()
      FXRenderer(simpleFull(), smallDim, smallDim)
    }
  }

}
