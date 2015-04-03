package margn.main

import margn.compiler.SCompiler

import scala.io.Source

object Margn {
  val extension = ".mg"
  def main(args: Array[String]) = {
    if (args.length == 0) {
      println("Usage: margn <source>")
    }
    else {
      val filename = args(0)
      if (!filename.endsWith(extension)) {
        println("Invalid filename")
        println("Program will exit.")
        sys.exit(-1)
      }
      val classFile = filename.substring(0, filename.indexOf(extension))

      val code = Source.fromFile(filename).getLines().mkString("\n")

      println(s"Compiling $filename to $classFile")
      SCompiler.compile(classFile, code)
      println("Successfully compiled")
    }
  }
}
