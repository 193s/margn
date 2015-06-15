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

  // stat + split
  def statement: Stat = stat <~ split
  def stat: Stat = block | print | assert | let | if_ | pass | failure("<statement>")
  def split = "[;\n]+".r

  def block:  Stat = "{" ~> program <~ "}" ^^ { p => ASTBlock(p.children) }
  def print:  Stat = "print"  ~> expr ^^ { ASTPrint }
  def assert: Stat = "assert" ~> expr ^^ { ASTAssert }
  def let:    Stat = "let" ~> ident ~ "=" ~ expr ^^ {
    case id ~ _ ~ expr => ASTLet(id, expr)
  }
  def if_ :   Stat = "if" ~> expr ~ ":" ~ stat ~ ( split ~> "else" ~> ":" ~> stat ).? ^^ {
    case astCond ~ _ ~ astThen ~ opt =>
    val elseOpt = opt
    if (elseOpt.isEmpty) ASTIf    (astCond, astThen)
    else                 ASTIfElse(astCond, astThen, elseOpt.get)
  }
  def pass:   Stat = "pass" ^^ { _ => ASTPass() }



  /* expr parsers */

  def expr: Expr = e0

  def unaryOp: Expr = "-" ~> simpleExpr ^^ { ASTIUnaryMinus }

  def simpleExpr: Expr = (
    unaryOp
  | literal
  | variable
  | "(" ~> expr <~ ")"
  | failure("<simpleExpr>")
  )

  def variable: Expr = ident ^^ { ASTVariableReference }


  // op_level -> (op -> ref)
  private val ops: Map[Int, Map[String, (ASTExpr, ASTExpr) => ASTOperator]] = Map (
    0 -> Map (
      "and" -> ASTAnd,
      "or"  -> ASTOr,
      "^"   -> ASTXor
    ),
    1 -> Map (
      "==" -> AST_EQ,
      "!=" -> AST_NE,

      ">=" -> AST_GE,
      ">"  -> AST_GT,

      "<=" -> AST_LE,
      "<"  -> AST_LT
    ),
    2 -> Map (
      "+"  -> ASTPlus,
      "-"  -> ASTMinus
    ),
    3 -> Map (
      "*"  -> ASTMultiply,
      "/"  -> ASTDivide
    ),
    4 -> Map(),
    5 -> Map(),
    6 -> Map(),
    7 -> Map(),
    8 -> Map(),
    9 -> Map(),
    10 -> Map()
  )

  private def biOp(priority: Int)(op: String)(left: ASTExpr, right: ASTExpr): ASTOperator = ops(priority)(op)(left, right)
  // List(a, b, c, ...) -> Parser(a | b | c, ...)
  private def string_or_parser(list: List[String]): Parser[String] = {
    // list.reduceLeft((l: Parser[String], r: String) => l | r)
    var ret: Parser[String] = list.head
    for (s <- list.tail) ret = ret | s
    ret
  }
  private def op(priority: Int): Parser[String] = string_or_parser(ops(priority).keys.toList)

  def e0: Expr = e1 ~ op(0) ~ e0 ^^ {
    case left ~ op ~ right => biOp(0)(op)(left, right)
  } | e1

  def e1: Expr = e2 ~ op(1) ~ e1 ^^ {
    case left ~ op ~ right => biOp(1)(op)(left, right)
  } | e2

  def e2: Expr = e3 ~ op(2) ~ e2 ^^ {
    case left ~ op ~ right => biOp(2)(op)(left, right)
  } | e3

  def e3: Expr = e4 ~ op(3) ~ e3 ^^ {
    case left ~ op ~ right => biOp(3)(op)(left, right)
  } | e4
  def e4 = e5
  def e5 = e6
  def e6 = e7
  def e7 = e8
  def e8 = e9
  def e9 = e10
  def e10 = simpleExpr




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

