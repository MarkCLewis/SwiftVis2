package swiftvis2

class DataElement(
    private val _x: Array[Double],
    private val _s: Array[String],
    private val _k: Array[Int]) {
  def x(index: Int): Double = _x(index)
  def s(index: Int): String = _s(index)
  def k(index: Int): Int = _k(index)
}

object DataElement {
  val noNums = Array[Double]()
  val noStrings = Array[String]()
  val noInts = Array[Int]()

  def apply(nums: Seq[Double] = noNums, strings: Seq[String] = noStrings, ints: Seq[Int] = noInts): DataElement = 
    new DataElement(nums.toArray, strings.toArray, ints.toArray)
}