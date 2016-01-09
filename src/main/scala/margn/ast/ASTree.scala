package margn.ast


// == Statement == //
case class ASTBlock(children: Seq[ASTStatement]) extends ASTStatement

case class ASTPass()                extends ASTStatement
case class ASTPrint(expr: ASTExpr)  extends ASTStatement
case class ASTAssert(expr: ASTExpr) extends ASTStatement
case class ASTLet(id: String, expr: ASTExpr) extends ASTStatement
case class ASTIf(cond: ASTExpr, then: ASTStatement) extends ASTStatement
case class ASTIfElse(cond: ASTExpr, then: ASTStatement, else_ : ASTStatement) extends ASTStatement

// == Expr == //
case class ASTVariableReference(id: String) extends ASTExpr

abstract class ASTLiteral extends ASTExpr
//case class ASTRef(value: String)      extends ASTLiteral
case class ASTInteger(value: Int)     extends ASTLiteral
case class ASTBoolean(value: Boolean) extends ASTLiteral
case class ASTString(value: String)   extends ASTLiteral

abstract class ASTOperator extends ASTExpr
abstract class ASTBiOperator(left: ASTExpr, right: ASTExpr) extends ASTOperator

abstract class ASTCompare(left: ASTExpr, right: ASTExpr) extends ASTBiOperator(left, right)
case class ASTAnd(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class ASTOr (left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)

case class AST_EQ(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_NE(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_GE(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_GT(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_LE(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)
case class AST_LT(left: ASTExpr, right: ASTExpr) extends ASTCompare(left, right)

case class ASTUnaryPlus(expr: ASTExpr)        extends ASTOperator
case class ASTUnaryMinus(expr: ASTExpr)       extends ASTOperator
case class ASTUnaryTilda(expr: ASTExpr)       extends ASTOperator
case class ASTUnaryExclamation(expr: ASTExpr) extends ASTOperator

case class ASTPlus(left: ASTExpr, right: ASTExpr)     extends ASTOperator
case class ASTMinus(left: ASTExpr, right: ASTExpr)    extends ASTOperator
case class ASTMultiply(left: ASTExpr, right: ASTExpr) extends ASTOperator
case class ASTDivide(left: ASTExpr, right: ASTExpr)   extends ASTOperator
case class ASTXor(left: ASTExpr, right: ASTExpr)      extends ASTOperator
case class ASTRefOp(left: ASTExpr, right: ASTExpr)    extends ASTOperator

