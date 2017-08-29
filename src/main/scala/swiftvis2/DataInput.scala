package swiftvis2

/**
 * This class is used to represent the input data coming into a filter.
 */
class DataInput private (_d: IndexedSeq[DataSet]) {
  def x(row: Int, col: Int) = _d(0).x(row, col)
  def s(row: Int, col: Int) = _d(0).s(row, col)
  def k(row: Int, col: Int) = _d(0).k(row, col)
  def d(whichSet: Int) = _d(whichSet)
}

object DataInput {
  def apply(isds: IndexedSeq[DataSet]) = new DataInput(isds)
}