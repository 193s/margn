package margn.ast

abstract class ASTree
case class ASTProgram(children: List[ASTStatement]) extends ASTree

abstract class ASTStatement extends ASTree

abstract class ASTExpr extends ASTree
