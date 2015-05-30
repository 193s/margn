package margn.parser

import margn.ast._

import scala.util.parsing.combinator.JavaTokenParsers


object Parser extends JavaTokenParsers {
  type Prog = Parser[ASTProgram]
  type Stat = Parser[ASTStatement]
  type Expr = Parser[ASTExpr]

  def apply(str: String): ASTProgram =
    parseAll(program, str) match {
      case Success(res, next) => res
      case NoSuccess(err, next) => throw new ParseError(err)
    }

  // sequence of statement
  def program: Prog = statement.* ^^ { ASTProgram }

  /* statement parsers */

  def print:  Stat = "print"  ~> expr ^^ { ASTPrint }
  def assert: Stat = "assert" ~> expr ^^ { ASTAssert }
  def let:    Stat = "let" ~> ident ~ "=" ~ expr ^^ {
    case left ~ _ ~ expr => ASTLet(left, expr)
  }
  def if_ :   Stat = "if" ~> expr ~ ":" ~ statement ~ ( "else" ~> ":" ~> statement ).? ^^ {
    case astCond ~ _ ~ astThen ~ opt =>
    val elseOpt = opt
    if (elseOpt.isEmpty) ASTIf    (astCond, astThen)
    else                 ASTIfElse(astCond, astThen, elseOpt.get)
  }
  def pass:   Stat = "pass" ^^ { _ => ASTPass() }

  def split = "[;\n]+".r
  def statement: Stat = (print | assert | let | if_ | pass | failure("<statement>")) <~ split

  /* expr parsers */


  def simpleExpr: Expr = (
    "-" ~> simpleExpr ^^ { ASTIUnaryMinus }
  | literal
  | variable
  | "(" ~> expr <~ ")"
  )

  def e0: Expr = e1 ~ ("=="|"!="|">="|">"|"<="|"<") ~ e0 ^^ {
    case left ~ op ~ right =>
      op match {
        case "==" => ASTEquals(left, right)
        case "!=" => ASTNotEquals(left, right)
        case ">=" => ASTGreaterThanOrEquals(left, right)
        case ">"  => ASTGreaterThan(left, right)
        case "<=" => ASTLessThanOrEquals(left, right)
        case "<"  => ASTLessThan(left, right)
      }
  } | e1

  def e1: Expr = e2 ~ ("+"|"-") ~ e1 ^^ {
    case left ~ op ~ right =>
      op match {
        case "+" => ASTIAdd(left, right)
        case "-" => ASTISub(left, right)
      }
  } | e2

  def e2: Expr = e3 ~ ("*"|"/") ~ e2 ^^ {
    case left ~ op ~ right =>
      op match {
        case "*" => ASTIMul(left, right)
        case "/" => ASTIDiv(left, right)
      }
  } | e3

  def e3 = e4
  def e4 = e5
  def e5 = e6
  def e6 = e7
  def e7 = e8
  def e8 = e9
  def e9 = e10
  def e10 = simpleExpr

  def expr: Expr = e0
  def variable: Expr = ident ^^ { ASTVariableReference }



  /* literals */
  def literal: Expr = integerLiteral | stringLit | booleanLiteral

  def integerLiteral: Expr = (
    binaryNumeral
      | hexNumeral
      | decimalNumeral
      | "0" ^^^ ASTInteger(0)
    )

  // parseInt
  private def parseIntWithParseError(s: String, base: Int) =
    try ASTInteger(Integer.parseInt(s, base))
    catch {
      case e: NumberFormatException =>
        throw new ParseError("Integer number too large: " + s)
    }
  def binaryNumeral : Expr = "0b" ~> "[01]+".r        ^^ { parseIntWithParseError(_, 2) }
  def hexNumeral    : Expr = "0x" ~> "[0-9a-fA-F]+".r ^^ { parseIntWithParseError(_, 16) }
  def decimalNumeral: Expr = "[1-9][0-9]*".r          ^^ { parseIntWithParseError(_, 10) }


  private val p_quote = "\"(.*)\"".r
  def stringLit: Expr = stringLiteral ^^ {
    case p_quote(s) => ASTString(s)
    case s          => ASTString(s)
  }

  def booleanLiteral: Expr = ("true" ^^^ true | "false" ^^^ false) ^^ { ASTBoolean }

}

