package margn.types


// Data Types (enum)
object DType {
  // singleton objects
  case object Integer extends DType
  case object String  extends DType
}

sealed abstract class DType
