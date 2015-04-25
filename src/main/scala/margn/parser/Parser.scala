package margn.parser

import margn.ast._

import scala.util.parsing.combinator.RegexParsers


object Parser extends RegexParsers {
  type Program = Parser[ASTProgram]
  type Statement = Parser[ASTStatement]
  type Expr = Parser[ASTExpr]

  def apply(str: String): ASTProgram = {
    val opt = parseAll(program, str)
    if (opt.isEmpty) throw new ParseError("failed to parse program")
    opt.get
  }

  def program: Program = (statement ~ ";").* ^^ { t => ASTProgram(t.map(_._1)) }

  /* statement parsers */

  def print:  Statement = "print"  ~> expr ^^ { ASTPrint }
  def assert: Statement = "assert" ~> expr ^^ { ASTAssert }
  def let:    Statement = "let" ~> "[a-zA-Z_]+".r ~ "=" ~ expr ^^ {
    case left ~ _ ~ expr => ASTLet(left, expr)
  }
  def if_ :   Statement = "if" ~> expr ~ ":" ~ statement ~ ( "else" ~> ":" ~> statement ).? ^^ {
    case astCond ~ _ ~ astThen ~ opt =>
    val elseOpt = opt
    if (elseOpt.isEmpty) ASTIf    (astCond, astThen)
    else                 ASTIfElse(astCond, astThen, elseOpt.get)
  }
  def statement: Statement = print | assert | let | if_

  /* expr parsers */

  def integerLiteral: Expr = (
    binaryNumeral
    | hexNumeral
    | decimalNumeral
    | "0" ^^^ ASTIntegerLiteral(0)
    )

  def binaryNumeral: Expr  = "0b" ~> "[01]+".r ^^ { s =>
    try ASTIntegerLiteral(Integer.parseInt(s, 2))
    catch {
      case e: NumberFormatException =>
        throw new ParseError("Integer number too large: " + s)
    }
  }
  def hexNumeral: Expr     = "0x" ~> "[0-9a-fA-F]+".r ^^ { s =>
    try ASTIntegerLiteral(Integer.parseInt(s, 16))
    catch {
      case e: NumberFormatException =>
        throw new ParseError("Integer number too large: " + s)
    }
  }
  def decimalNumeral: Expr = "[1-9][0-9]*".r ^^ { s =>
    try ASTIntegerLiteral(Integer.parseInt(s, 10))
    catch {
      case e: NumberFormatException =>
        throw new ParseError("Integer number too large: " + s)
    }
  }

  def variable: Expr = "[a-zA-Z_]+".r ^^ { ASTVariableReference }

  def simpleExpr: Expr = (
    "-" ~> simpleExpr ^^ { ASTIUnaryMinus }
  | integerLiteral
  | variable
  | "(" ~> expr <~ ")"
  )

  def e0: Expr = e1 ~ ("=="|"!=") ~ e0 ^^ {
    case left ~ op ~ right =>
      op match {
        case "==" => ASTEquals(left, right)
        case "!=" => ASTNotEquals(left, right)
      }
  } | e1

  def e1: Expr = e2 ~ ("+"|"-") ~ e1 ^^ {
    case left ~ op ~ right =>
      op match {
        case "+" => ASTIAdd(left, right)
        case "-" => ASTISub(left, right)
      }
  } | e2

  def e2: Expr = simpleExpr ~ ("*"|"/") ~ e2 ^^ {
    case left ~ op ~ right =>
      op match {
        case "*" => ASTIMul(left, right)
        case "/" => ASTIDiv(left, right)
      }
  } | simpleExpr

  def expr: Expr = e0
}

