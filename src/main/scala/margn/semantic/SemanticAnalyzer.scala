package margn.semantic

import margn.ast._
import margn.types.DType
import margn.types.DType.{DBool, DInt, DString}

import scala.collection.mutable

object SemanticAnalyzer {
  def apply(program: ASTProgram): Unit = SAnalyzer.analyze(program)

  class Env() {
    private var maxIndex = 0
    private val namespace = mutable.Map[String, (Int, DType)]()

    def createIndex(name: String, _type: DType): Int = {
      val index = maxIndex
      namespace += name -> (index, _type)
      maxIndex += 1
      index
    }
    def resolve(name: String) =
      if (namespace.contains(name)) namespace(name)
      else throw new NameError("couldn't resolve name: " + name)

    def getIndex(name: String) = resolve(name)._1
    def getType (name: String) = resolve(name)._2
  }


  object SAnalyzer {
    /** SA on given AST */
    def analyze(program: ASTProgram): Unit = {
      val env = new Env
      for (s: ASTStatement <- program.children) {
        checkStatement(s, env)
      }
    }

    private def typeError(msg: String = "") = throw new TypeError(msg)

    /** compile expressions */
    def checkExpr(ast: ASTExpr, env: Env): DType = {
      ast._type_ = ast match {
        // int
        case ASTInteger(value) => DInt
        // bool
        case ASTBoolean(value) => DBool
        // string
        case ASTString(string) => DString
        // variable
        case ASTVariableReference(id) => env.getType(id)

        // + expr
        case ASTUnaryPlus(expr) =>
          checkExpr(expr, env) match {
            case DInt => DInt
            case any  => typeError(s"+ <int> : $any")
          }

        // - expr
        case ASTUnaryMinus(expr) =>
          checkExpr(expr, env) match {
            case DInt => DInt
            case any  => typeError(s"- <int> : $any")
          }

        // ~ expr
        case ASTUnaryTilda(expr) =>
          checkExpr(expr, env) match {
            case DInt => DInt
            case any  => typeError(s"~ <int> : $any")
          }

        // ! expr
        case ASTUnaryExclamation(expr) =>
          checkExpr(expr, env) match {
            case DBool => DBool
            case any   => typeError(s"- <bool> : $any")
          }

        // expr + expr
        case ASTPlus(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DInt
            // case (DString, DInt) => TODO
            case any          => typeError(s"<int> + <int> : $any")
          }

        // expr - expr
        case ASTMinus(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DInt
            case any          => typeError(s"<int> - <int> : $any")
          }

        // expr * expr
        case ASTMultiply(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DInt
            case any          => typeError(s"<int> * <int> : $any")
          }

        // expr / expr
        case ASTDivide(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DInt
            case any          => typeError(s"<int> / <int> : $any")
          }

        // expr and expr
        case ASTAnd(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DBool, DBool) => DBool
            case any            => typeError(s"<bool> and <bool> : $any")
          }

        // expr or expr
        case ASTOr(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DBool, DBool) => DBool
            case any            => typeError(s"<bool> or <bool> : $any")
          }

        // expr or expr
        case ASTXor(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DInt
            case any          => typeError(s"<int> ^ <int> : $any")
          }

        // expr == expr
        case AST_EQ(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DBool
            case any => typeError(s"<int> == <int> : $any")
          }

        // expr != expr
        case AST_NE(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DBool
            case any => typeError(s"<int> != <int> : $any")
          }

        // expr >= expr
        case AST_GE(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DBool
            case any => typeError(s"<int> >= <int> : $any")
          }

        // expr > expr
        case AST_GT(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DBool
            case any => typeError(s"<int> > <int> : $any")
          }

        // expr <= expr
        case AST_LE(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DBool
            case any => typeError(s"<int> <= <int> : $any")
          }

        // expr < expr
        case AST_LT(left, right) =>
          (checkExpr(left, env), checkExpr(right, env)) match {
            case (DInt, DInt) => DBool
            case any => typeError(s"<int> < <int> : $any")
          }

        case e => throw new SAError(s"[FATAL ERROR] Unexpected syntax tree (expr): $e")
      }
      ast._type_
    }

    /** compile statements */
    def checkStatement(ast: ASTStatement, env: Env): Unit = {
      ast match {
        // { block }
        case ASTBlock(children) =>
          for (stat <- children) checkStatement(stat, env)

        // print
        case ASTPrint(expr) => checkExpr(expr, env)

        // assert
        case ASTAssert(expr) =>
          checkExpr(expr, env) match {
            case DBool =>
            case any => typeError(s"assert <bool> : $any")
          }

        // let
        case ASTLet(id, expr) =>
          env.createIndex(id, checkExpr(expr, env))

        // if
        case ASTIf(cond, then) =>
          checkExpr(cond, env)
          checkStatement(then, env)

        // if - else
        case ASTIfElse(cond, then, els) =>
          checkExpr(cond, env)
          checkStatement(then, env)
          checkStatement(els , env)

        case e => throw new SAError(s"[FATAL ERROR] Unexpected syntax tree (statement): $e")
      }
    }
  }
}
