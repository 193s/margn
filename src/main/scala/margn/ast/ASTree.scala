package margn.ast

import margn.types.DType
import margn.types.DType._


// == Statement == //
case class ASTBlock(children: Seq[ASTStatement]) extends ASTStatement

case class ASTPass()                extends ASTStatement
case class ASTPrint(expr: ASTExpr)  extends ASTStatement
case class ASTAssert(expr: ASTExpr) extends ASTStatement
case class ASTLet(id: String, expr: ASTExpr) extends ASTStatement
case class ASTIf(cond: ASTExpr, then: ASTStatement) extends ASTStatement
case class ASTIfElse(cond: ASTExpr, then: ASTStatement, else_ : ASTStatement) extends ASTStatement

// == Expr == //
case class ASTVariableReference(id: String) extends ASTExpr(DInteger)

abstract class ASTLiteral(_type_ : DType) extends ASTExpr(_type_)
case class ASTInteger(value: Int)     extends ASTLiteral(DInteger)
case class ASTBoolean(value: Boolean) extends ASTLiteral(DBoolean)
case class ASTString(value: String)   extends ASTLiteral(DString)

abstract class ASTOperator extends ASTExpr(DInteger)
case class ASTEquals(left: ASTExpr, right: ASTExpr)              extends ASTOperator
case class ASTNotEquals(left: ASTExpr, right: ASTExpr)           extends ASTOperator
case class ASTGreaterThanOrEquals(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTGreaterThan(left: ASTExpr, right: ASTExpr)         extends ASTOperator
case class ASTLessThanOrEquals(left: ASTExpr, right: ASTExpr)    extends ASTOperator
case class ASTLessThan(left: ASTExpr, right: ASTExpr)            extends ASTOperator

case class ASTIUnaryMinus(expr: ASTExpr) extends ASTOperator
case class ASTIAdd(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTISub(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTIMul(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTIDiv(left: ASTExpr, right: ASTExpr) extends ASTOperator

