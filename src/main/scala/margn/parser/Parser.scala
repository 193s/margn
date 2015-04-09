package margn.parser

import margn.ast._

import scala.util.parsing.combinator.RegexParsers


object ProgramParser extends RegexParsers {
  type T = Parser[ASTProgram]
  def apply(str: String): ASTProgram = {
    val program = ASTProgram(str.split(';').map(StatementParser(_)).toList)
    program
    /*
    val opt = parseAll(program, str)
    if (opt.isEmpty) throw new ParseError("failed to parse program")
    opt.get
    */
  }

  def program: T = repsep(".*".r ^^ { StatementParser(_) }, ";") ^^ { t => ASTProgram(t) }
}

object StatementParser extends RegexParsers {
  type T = Parser[ASTStatement]
  def apply(str: String): ASTStatement = {
    parseAll(statement, str) match {
      case Success(res, next) => res
      case NoSuccess(err, next) =>
        // throw new ParseError(s"$err on line ${next.pos.line} on column ${next.pos.column}")
       throw new ParseError(err)
    }
  }

  def print: T = "print" ~> ".*".r ^^ { s => ASTPrint(ExprParser(s)) }
  def let:   T = "let" ~> "[a-zA-Z_]+".r ~ "=" ~ ".*".r ^^ { t => ASTLet(t._1._1, ExprParser(t._2)) }
  def if_ :  T = "if" ~> "[^:]+".r ~ ":" ~ "[^;]+".r  ~ ( "else" ~> ".+".r ).? ^^ { t =>
    val astCond = ExprParser(t._1._1._1)
    val astThen = StatementParser(t._1._2)
    val elseOpt = t._2
    if (elseOpt.isEmpty) ASTIf(astCond, astThen)
    else                 ASTIfElse(astCond, astThen, StatementParser(elseOpt.get))
  }
  def statement: T = print | let | if_
}

object ExprParser extends RegexParsers {
  type T = Parser[ASTExpr]
  def apply(str: String): ASTExpr = {
    parseAll(expr, str) match {
      case Success(res, next) => res
      case NoSuccess(err, next) =>
        // throw new ParseError(s"$err on line ${next.pos.line} on column ${next.pos.column}")
        throw new ParseError(err)
    }
  }

  def integerLiteral: T = (
    binaryNumeral
    | hexNumeral
    | decimalNumeral
    | "0" ^^^ ASTIntegerLiteral(0)
    )

  def binaryNumeral: T  = "0b" ~> "[01]+".r ^^ { s =>
    try ASTIntegerLiteral(Integer.parseInt(s, 2))
    catch {
      case e: NumberFormatException =>
        throw new ParseError("Integer number too large: " + s)
    }
  }
  def hexNumeral: T     = "0x" ~> "[0-9a-fA-F]+".r ^^ { s =>
    // FIXME: Parse error
    ASTIntegerLiteral(Integer.parseInt(s, 16))
  }
  def decimalNumeral: T = "[1-9][0-9]*".r ^^ { s =>
    // FIXME: Parser error
    ASTIntegerLiteral(Integer.parseInt(s, 10))
  }

  def variable: T = "[a-zA-Z_]+".r ^^ { ASTVariableReference }

  def simpleExpr: T = (
    "-" ~> simpleExpr ^^ { ASTIUnaryMinus }
  | integerLiteral
  | variable
  | "(" ~> expr <~ ")"
  )

  def expr: T = (
    simpleExpr ~ "+" ~ expr ^^ {
      t => ASTIAdd(t._1._1, t._2)
    }
    | simpleExpr ~ "-" ~ expr ^^ {
      t => ASTISub(t._1._1, t._2)
    }
    | simpleExpr ~ "*" ~ expr ^^ {
      t => ASTIMul(t._1._1, t._2)
    }
    | simpleExpr ~ "/" ~ expr ^^ {
      t => ASTIDiv(t._1._1, t._2)
    }
    | simpleExpr ~ "==" ~ expr ^^ {
      t => ASTEquals(t._1._1, t._2)
    }
    | simpleExpr
  )
}

