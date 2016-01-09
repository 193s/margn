package margn.compiler

import margn.ast._
import margn.semantic.SemanticAnalyzer
import margn.parser.Parser
import margn.types.DType.{DBool, DInt, DString}
import org.apache.bcel.Constants._
import org.apache.bcel.generic._

import scala.collection.mutable


class Env(val cg: ConstantPoolGen) {
  private var maxIndex = 0
  private val namespace = mutable.Map[String, Int]()

  def createIndex(name: String): Int = {
    val index = maxIndex
    namespace += name -> index
    maxIndex += 1
    index
  }
  def getIndex(name: String) = {
    if (namespace.contains(name)) namespace(name)
    else throw new CompileError("couldn't resolve name: " + name)
  }
}


object SCompiler {
  private type IL = InstructionList
  /** compile source code into class file */
  def compile(classFile: String, code: String): Unit = {
    val ast = Parser(code)
    SemanticAnalyzer(ast)
    compile(classFile, ast)
  }

  private def compileError(msg: String = "") = throw new CompileError(msg)

  private val CONST_TRUE  = new ICONST(1)
  private val CONST_FALSE = new ICONST(0)
  /** compile expressions */
  def compileExpr(ast: ASTExpr, env: Env): IL = {
    val il = new IL()
    ast match {
      // int
      case ASTInteger(value) =>
        il.append(new PUSH(env.cg, value))

      // bool
      case ASTBoolean(value) =>
        il.append(new PUSH(env.cg, value))

      // string
      case ASTString(string) =>
        il.append(new PUSH(env.cg, string))

      // variable
      case ASTVariableReference(id) =>
        il.append(new ILOAD(env.getIndex(id)))

      // + expr
      case ASTUnaryPlus(expr) =>
        il.append(compileExpr(expr, env))

      // - expr
      case ASTUnaryMinus(expr) =>
        il.append(compileExpr(expr, env))
        il.append(new INEG())

      // ~ expr
      case ASTUnaryTilda(expr) =>
        compileError("unimplemented: ~ <int>")

      // ! expr
      case ASTUnaryExclamation(expr) =>
        il.append(compileExpr(expr, env))
        il.append(branchIns(
          new IFNE(null),
          new IL(CONST_TRUE),
          new IL(CONST_FALSE)
        ))

      // expr + expr
      case ASTPlus(left, right) =>
        (left._type_, right._type_) match {
          case (DInt, DInt) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new IADD())

          // TODO
          // case (DString, DInt) =>
          case any =>
        }

      // expr - expr
      case ASTMinus(left, right) =>
        (left._type_, right._type_) match {
          case (DInt, DInt) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new ISUB())
          case any =>
        }

      // expr * expr
      case ASTMultiply(left, right) =>
        (left._type_, right._type_) match {
          case (DInt, DInt) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new IMUL())
          case any =>
        }

      // expr / expr
      case ASTDivide(left, right) =>
        (left._type_, right._type_) match {
          case (DInt, DInt) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new IDIV())
          case any =>
        }

      // expr and expr
      case ASTAnd(left, right) =>
        (left._type_, right._type_) match {
          case (DBool, DBool) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new IAND())
          case any =>
        }

      // expr or expr
      case ASTOr(left, right) =>
        (left._type_, right._type_) match {
          case (DBool, DBool) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new IOR())
          case any =>
        }

      // expr or expr
      case ASTXor(left, right) =>
        (left._type_, right._type_) match {
          case (DInt, DInt) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(new IXOR())
          case any =>
        }

      // expr == expr
      case AST_EQ(left, right) =>
        (left._type_, right._type_) match {
          case (DInt, DInt) =>
            il.append(compileExpr(left, env))
            il.append(compileExpr(right, env))
            il.append(branchIns(
              new IF_ICMPEQ(null),
              new IL(CONST_FALSE),
              new IL(CONST_TRUE)
            ))
          case any =>
        }

      // expr != expr
      case AST_NE(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPNE(null),
          new IL(CONST_FALSE),
          new IL(CONST_TRUE)
        ))

      // expr >= expr
      case AST_GE(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPGE(null),
          new IL(CONST_FALSE),
          new IL(CONST_TRUE)
        ))

      // expr > expr
      case AST_GT(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPGT(null),
          new IL(CONST_FALSE),
          new IL(CONST_TRUE)
        ))

      // expr <= expr
      case AST_LE(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPLE(null),
          new IL(CONST_FALSE),
          new IL(CONST_TRUE)
        ))

      // expr < expr
      case AST_LT(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPLT(null),
          new IL(CONST_FALSE),
          new IL(CONST_TRUE)
        ))

      case e => throw new CompileError(s"[FATAL ERROR] Unexpected syntax tree (expr): $e")
    }
    il
  }

  /** branch instruction: if-then style */
  private def branchIns(branch_ins: IfInstruction, then: IL): IL = {
    val il = new IL()
    // branch
    val target = il.append(branch_ins)
    // then
    il.append(then)
    val ih = il.append(new NOP())
    // set target
    target.setTarget(ih)
    il
  }

  /** branch instruction: if-then-else style */
  private def branchIns(branch_ins: IfInstruction, then: IL, els: IL): IL = {
    val il = new IL()
    // branch
    val target = il.append(branch_ins)
    // then
    il.append(then)
    val t2 = il.append(new GOTO(null))
    // else
    il.append(els)
    val ih = il.append(new NOP())
    // set target
    target.setTarget(t2.getNext)
    t2.setTarget(ih)
    il
  }

  /** compile statements */
  def compileStatement(ast: ASTStatement, env: Env): IL = {
    val il = new IL()
    ast match {
      case ASTBlock(children) =>
        // compile and append all statements
        for (stat <- children) il.append(compileStatement(stat, env))

      case ASTPass() =>
        // null operation
        il.append(new NOP())

      // print
      case ASTPrint(expr) =>
        val out = env.cg.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")
        val t = expr._type_ match {
          case DInt     => Type.INT
          case DString  => Type.STRING
          case DBool    => Type.BOOLEAN
          case any      => Type.OBJECT
        }
        val sig = "(" + t.getSignature + ")V"
        val sys_println = env.cg.addMethodref("java.io.PrintStream", "println", sig)
        il.append(new GETSTATIC(out))
        il.append(compileExpr(expr, env))
        il.append(new INVOKEVIRTUAL(sys_println))

      // assert
      case ASTAssert(expr) =>
        il.append(compileExpr(expr, env)) // check assertion
        val assert_err = env.cg.addClass("java/lang/AssertionError")
        val new_assert =
          env.cg.addMethodref("java/lang/AssertionError", "<init>", "(Ljava/lang/Object;)V")

        il.append(branchIns(
          new IFNE(null),
          {
            val l = new IL()
            l.append(new NEW(assert_err))
            l.append(new DUP())
            l.append(new LDC(env.cg.addString("assertion failed")))
            l.append(new INVOKESPECIAL(new_assert))
            l.append(new ATHROW())
            l
          }
        ))

      // let
      case ASTLet(id, expr) =>
        val index = env.createIndex(id)
        il.append(compileExpr(expr, env))
        il.append(new ISTORE(index))

      // if
      case ASTIf(cond, then) =>
        il.append(compileExpr(cond, env))
        il.append(branchIns(
          new IFEQ(null),
          compileStatement(then, env)
        ))

      // if - else
      case ASTIfElse(cond, then, els) =>
        il.append(compileExpr(cond, env))
        il.append(branchIns(
          new IFEQ(null),
          compileStatement(then, env),
          compileStatement(els, env)
        ))

      case e => throw new CompileError(s"[FATAL ERROR] Unexpected syntax tree (statement): $e")
    }
    il
  }

  /** compile programs */
  def compileProgram(ast: ASTProgram, env: Env): IL = {
    val il = new IL()
    for (s: ASTStatement <- ast.children) {
      il.append(compileStatement(s, env))
    }
    il
  }

  /** compile AST and dump to classFile */
  def compile(classFile: String, program: ASTProgram): Unit = {
    val gen = new ClassGen(classFile, "java.lang.Object", "<margn>", ACC_PUBLIC, null)
    val cg = gen.getConstantPool
    val env = new Env(cg)

    // generate instruction list
    val il = new IL()
    il.append(compileProgram(program, env))
    // return (void)
    il.append(InstructionConstants.RETURN)

    // main method
    val mg = new MethodGen (
      ACC_PUBLIC | ACC_STATIC, Type.VOID,
      Array[Type] ( new ArrayType(Type.STRING, 1) ),
      Array("args"), "main",
      classFile, il, cg
    )
    mg.setMaxStack()
    mg.setMaxLocals()

    gen.addMethod(mg.getMethod)
    // dump
    gen.getJavaClass.dump(classFile + ".class")
  }
}
