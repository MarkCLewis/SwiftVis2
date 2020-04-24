package swiftvis2.plotting.renderer
import scalanative.native._
import scalanative.native.stdlib._
import java.nio.{ByteBuffer, ByteOrder}
import scalanative.native.stdio._
import swiftvis2.plotting._


object NativeRingRenderer extends App {
  def read() = {
    val stream = fopen(c"/home/nick/Downloads/CartAndRad.88840.bin", c"rb")

    val nBuff = stackalloc[Byte](4)

    fread(nBuff, 1, 4, stream)

    val byteBuff = ByteBuffer.wrap(Array(nBuff(3), nBuff(2), nBuff(1), nBuff(0)))

    val n = byteBuff.getInt()

    val doubleBuff = malloc(n * 7 * 8)

    fread(doubleBuff, 1, n * 7 * 8, stream)

    val arr = new Array[Byte](n * 7 * 8)

    for(i <- arr.indices) arr(i) = doubleBuff(i)

    val doublebytebuff = ByteBuffer.wrap(arr)

    doublebytebuff.order(ByteOrder.LITTLE_ENDIAN)

    val xs = (0 until 2000000).map(i => doublebytebuff.getDouble(i*8*6))
    val ys = (0 until 2000000).map(i => doublebytebuff.getDouble(i*8*6 + 8))
    val rs = (0 until 2000000).map(i => doublebytebuff.getDouble(n*8*6 + i*8))
    fclose(stream)
    (xs, ys, rs)
  }
  val (xs, ys, rs) = read()

  val plot = Plot.scatterPlot(xs, ys, "Sim", "Radial",
    "Azimuthal", rs, xSizing = PlotSymbol.Sizing.Scaled, ySizing = PlotSymbol.Sizing.Scaled)
    .updatedAxis[NumericAxis]("x", axis => axis.copy(min =
      Some(-0.19), max = Some(-0.14), tickLabelInfo = axis.tickLabelInfo.map(_.copy(numberFormat = "%1.2f"))))
    .updatedAxis[NumericAxis]("y", axis => axis.copy(min =
      Some(100.77), max = Some(100.72), tickLabelInfo = axis.tickLabelInfo.map(_.copy(numberFormat = "%1.2f"))))

  SDLRenderer(plot, 1200, 1000)
//  val vs = new Array[Double](n * 6)

//  for(i <- vs.indices) {println(i);vs(i) = doublebbuff.getDouble()}
//
//  val rs = new Array[Double](n)
//
//  for(i <- rs.indices) rs(i) = doublebbuff.getDouble()
}
