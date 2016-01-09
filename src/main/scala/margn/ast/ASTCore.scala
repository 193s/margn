package margn.ast

import margn.types.DType

abstract class ASTree
case class ASTProgram(children: Seq[ASTStatement]) extends ASTree

abstract class ASTStatement extends ASTree

abstract class ASTExpr extends ASTree {
  var _type_ : DType = null
}

