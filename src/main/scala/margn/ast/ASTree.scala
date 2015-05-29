package margn.ast

import margn.types.DType


// == Statement == //
case class ASTPass()                extends ASTStatement
case class ASTPrint(expr: ASTExpr)  extends ASTStatement
case class ASTAssert(expr: ASTExpr) extends ASTStatement
case class ASTLet(id: String, expr: ASTExpr) extends ASTStatement
case class ASTIf(cond: ASTExpr, then: ASTStatement) extends ASTStatement
case class ASTIfElse(cond: ASTExpr, then: ASTStatement, else_ : ASTStatement) extends ASTStatement

// == Expr == //
case class ASTVariableReference(id: String) extends ASTExpr(DType.Integer)

abstract class ASTLiteral(_type_ : DType) extends ASTExpr(_type_)
case class ASTInteger(value: Int)    extends ASTLiteral(DType.Integer)
case class ASTString(string: String) extends ASTLiteral(DType.String)

abstract class ASTOperator extends ASTExpr(DType.Integer)
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

