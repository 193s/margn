package margn.compiler

import margn.ast._
import margn.parser.ProgramParser
import org.apache.bcel.generic._
import org.apache.bcel.Constants._

import scala.collection.mutable

object SCompiler {
  /** compile source code into class file */
  def compile(classFile: String, code: String): Unit = compile(classFile, ProgramParser(code))

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
        // push 0/1
        val t1 = il.append(new IF_ICMPEQ(null))
        il.append(new ICONST(0))          // push 0
        val t2 = il.append(new GOTO(null))
        // T1:
        val ih = il.append(new ICONST(1)) // push 1
        val ih2 = il.append(new NOP())
        t1.setTarget(ih)
        t2.setTarget(ih2)
        // T2: FIN

      case e => throw new CompileError(s"[FATAL ERROR] Unexpected syntax tree (expr): $e")
    }
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

      // let
      case ASTLet(id, expr) =>
        val index = env.createIndex(id)
        il.append(compileExpr(expr, env))
        il.append(new ISTORE(index))

      // if
      case ASTIf(cond, then) =>
        il.append(compileExpr(cond, env))
        val target = il.append(new IFEQ(null))
        il.append(compileStatement(then, env))
        val ih = il.append(new NOP())
        // set target
        target.setTarget(ih)

      // if - else
      case ASTIfElse(cond, then, else_) =>
        il.append(compileExpr(cond, env))
        val target = il.append(new IFEQ(null))
        il.append(compileStatement(then, env))
        val t2 = il.append(new GOTO(null))
        il.append(compileStatement(else_, env))
        val ih = il.append(new NOP())
        // set target
        target.setTarget(t2)
        t2.setTarget(ih)

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

    val il = new InstructionList()
    il.append(compileProgram(program, env))
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
