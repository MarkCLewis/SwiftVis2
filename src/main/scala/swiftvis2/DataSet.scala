package swiftvis2

class DataSet private (private val in: IndexedSeq[DataElement]) {
  
  def x(row: Int, col: Int) = in(row).x(col)
  def s(row: Int, col: Int) = in(row).s(col)
  def k(row: Int, col: Int) = in(row).k(col)
}

object DataSet {  
  def apply(xs: IndexedSeq[Array[Double]]): DataSet = {
    new DataSet(xs.map(x => DataElement(x)))
  }
}