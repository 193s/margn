package margn.ast


case class ASTPrint(expr: ASTExpr) extends ASTStatement
case class ASTLet(id: String, expr: ASTExpr) extends ASTStatement

case class ASTVariableReference(id: String) extends ASTExpr

abstract class ASTLiteral extends ASTExpr
case class ASTIntegerLiteral(value: Int) extends ASTLiteral

abstract class ASTOperator extends ASTExpr
case class ASTIUnaryMinus(expr: ASTExpr) extends ASTOperator
case class ASTIAdd(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTISub(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTIMul(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTIDiv(left: ASTExpr, right: ASTExpr) extends ASTOperator

