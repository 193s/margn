package margn.compiler

import margn.ast._
import margn.parser.Parser
import org.apache.bcel.generic._
import org.apache.bcel.Constants._

import scala.collection.mutable

object SCompiler {
  /** compile source code into class file */
  def compile(classFile: String, code: String): Unit = compile(classFile, Parser(code))

  /** compile expressions */
  def compileExpr(ast: ASTExpr, env: Env): InstructionList = {
    val il = new InstructionList()
    ast match {
      // integer literal
      case ASTIntegerLiteral(value) =>
        il.append(new PUSH(env.cg, value))

      // variable
      case ASTVariableReference(id) =>
        il.append(new ILOAD(env.getIndex(id)))

      // - expr
      case ASTIUnaryMinus(expr) =>
        // 0 - expr
        il.append(new PUSH(env.cg, 0))
        il.append(compileExpr(expr, env))
        il.append(new ISUB())

      // expr + expr
      case ASTIAdd(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        il.append(new IADD())

      // expr - expr
      case ASTISub(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        il.append(new ISUB())

      // expr * expr
      case ASTIMul(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        il.append(new IMUL())

      // expr / expr
      case ASTIDiv(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        il.append(new IDIV())

      // expr == expr
      case ASTEquals(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPEQ(null),
          new InstructionList(new ICONST(0)),
          new InstructionList(new ICONST(1))
        ))

      // expr != expr
      case ASTNotEquals(left, right) =>
        il.append(compileExpr(left, env))
        il.append(compileExpr(right, env))
        // branch: (l, r) -> [01]
        il.append(branchIns(
          new IF_ICMPNE(null),
          new InstructionList(new ICONST(0)),
          new InstructionList(new ICONST(1))
        ))

      case e => throw new CompileError(s"[FATAL ERROR] Unexpected syntax tree (expr): $e")
    }
    il
  }

  /** branch instruction: if-then style */
  def branchIns(branch_ins: IfInstruction, then: InstructionList) = {
    val il = new InstructionList()
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
  def branchIns(branch_ins: IfInstruction, then: InstructionList, els: InstructionList) = {
    val il = new InstructionList()
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
  def compileStatement(ast: ASTStatement, env: Env): InstructionList = {
    val il = new InstructionList()
    ast match {
      // print
      case ASTPrint(expr) =>
        val out = env.cg.addFieldref("java.lang.System", "out", "Ljava/io/PrintStream;")
        val println = env.cg.addMethodref("java.io.PrintStream", "println", "(I)V")
        il.append(new GETSTATIC(out))
        il.append(compileExpr(expr, env))
        il.append(new INVOKEVIRTUAL(println))

      // assert
      case ASTAssert(expr) =>
        il.append(compileExpr(expr, env)) // check assertion
        val assert_err = env.cg.addClass("java/lang/AssertionError")
        val new_assert =
          env.cg.addMethodref("java/lang/AssertionError", "<init>", "(Ljava/lang/Object;)V")

        il.append(branchIns(
          new IFNE(null),
          {
            val l = new InstructionList()
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
  def compileProgram(ast: ASTProgram, env: Env): InstructionList = {
    val il = new InstructionList()
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
    val il = new InstructionList()
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

class Env(val cg: ConstantPoolGen) {
  private var maxIndex = 0
  private val variableTable = mutable.Map[String, Int]()

  def createIndex(name: String): Int = {
    val index = maxIndex
    variableTable += name -> index
    maxIndex += 1
    index
  }
  def getIndex(name: String) = variableTable(name)
}
