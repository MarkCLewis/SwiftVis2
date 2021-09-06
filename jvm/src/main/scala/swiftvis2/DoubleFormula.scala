package swiftvis2

/**
 * This class represents a parsed and processes data formula.
 */
class DoubleFormula private (
  val form: String,
  val formTrait: CompilerParsing.FormTrait[Double],
  val bounds: CompilerParsing.FormulaBoundsInfo)
    extends ((DataInput, Int, Seq[Int]) => Double) {

  def specialsAndRange(di: DataInput, index: Int) = formTrait.specialsAndRange(di, index)
  def apply(di: DataInput, index: Int, specials: Seq[Int]) = formTrait(di, index, specials)
}

object DoubleFormula {
  def apply(formula: String): DoubleFormula = {
    val (f, g, gs) = CompilerParsing.decomposeFormula(formula)
    val (formTrait, bounds) = CompilerParsing.parse[Double](f, g, gs, "Double")
    new DoubleFormula(formula, formTrait, bounds)
  }
  

  def main(args: Array[String]): Unit = {
    val di = DataInput(Array(DataSet(Array.fill(100,3)(math.random())), DataSet(Array.fill(100,3)(math.random()))))
    val df = apply("x(i+1, 0)*d(1).x(i*2+3, 1)")
    println(df(di, 10, Nil))
    println(di.x(11,0)*di.d(1).x(23,1))
  }
}
