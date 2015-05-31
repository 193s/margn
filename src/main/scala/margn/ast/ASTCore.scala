package margn.ast

import margn.types.DType

abstract class ASTree
case class ASTProgram(children: Seq[ASTStatement]) extends ASTree

abstract class ASTStatement extends ASTree

abstract class ASTExpr(val _type_ : DType) extends ASTree


