package margn.types


// Data Types (enum)
object DType {
  // singleton objects
  case object DInt    extends DType
  case object DBool   extends DType
  case object DString extends DType
  case object DRef    extends DType
}

sealed abstract class DType
