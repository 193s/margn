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
case class ASTVariableReference(id: String) extends ASTExpr(DInt)

abstract class ASTLiteral(_type_ : DType) extends ASTExpr(_type_)
case class ASTInteger(value: Int)     extends ASTLiteral(DInt)
case class ASTBoolean(value: Boolean) extends ASTLiteral(DBool)
case class ASTString(value: String)   extends ASTLiteral(DString)

abstract class ASTOperator(_type_ : DType) extends ASTExpr(_type_)
abstract class ASTBiOperator(_type_ : DType)(left: ASTExpr, right: ASTExpr) extends ASTOperator(_type_)

abstract class ASTCompare(left: ASTExpr, right: ASTExpr) extends ASTBiOperator(DBool)(left, right)
case class ASTAnd(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class ASTOr (left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)

case class ASTXor(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)

case class AST_EQ(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_NE(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_GE(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_GT(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_LE(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_LT(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)

abstract class ASTIOperator extends ASTOperator(DInt)
case class ASTUnaryPlus(expr: ASTExpr)        extends ASTIOperator
case class ASTUnaryMinus(expr: ASTExpr)       extends ASTIOperator
case class ASTUnaryTilda(expr: ASTExpr)       extends ASTIOperator
case class ASTUnaryExclamation(expr: ASTExpr) extends ASTIOperator

case class ASTPlus(left: ASTExpr, right: ASTExpr)     extends ASTIOperator
case class ASTMinus(left: ASTExpr, right: ASTExpr)    extends ASTIOperator
case class ASTMultiply(left: ASTExpr, right: ASTExpr) extends ASTIOperator
case class ASTDivide(left: ASTExpr, right: ASTExpr)   extends ASTIOperator

