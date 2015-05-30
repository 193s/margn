package margn.types


// Data Types (enum)
object DType {
  // singleton objects
  case object DInteger extends DType
  case object DBoolean extends DType
  case object DString  extends DType
}

sealed abstract class DType
