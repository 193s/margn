package margn.main

import java.io.File
import margn.compiler.{CompileError, SCompiler}
import margn.parser.ParseError
import scala.io.Source

object Margn {
  val extension = ".mg"
  def main(args: Array[String]) = {
    optParser.parse(args, OptConfig()) match {
      case Some(opt_config) =>
      // do stuff
        val in = opt_config.in
        val verbose = opt_config.verbose

        val filename = in.getName
        // debug: print filename
        if (verbose) println(filename)

        // check filename extensions
        if (!filename.endsWith(extension)) {
          println(s"Invalid filename: filename must end with $extension")
          println("Program will exit.")
          sys.exit(-1)
        }

        // strip $extension
        val classFile = filename.substring(0, filename.indexOf(extension))

        val code = Source.fromFile(in).getLines().mkString("\n")

        println(s"Compiling $filename to $classFile")
        try SCompiler.compile(classFile, code)
        catch {
          // Parse Error
          case e: ParseError =>
            System.err.println("parser error:")
            System.err.println(e.getMessage)

          case e: CompileError =>
            System.err.println("compile error:")
            System.err.println(e.getMessage)
        }
        println("Successfully compiled")

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  case class OptConfig(verbose: Boolean = false, in: File = new File("."))
  // command line option parser
  val optParser = new scopt.OptionParser[OptConfig]("margn") {
    head("margn", "1.0")
    opt[Unit]("verbose") action { (_, c) =>
      c.copy(verbose = true) } text "verbose (unimplemented)"
    help("help") text "prints this usage text"
    arg[File]("<file>") required() action { (x, c) =>
      c.copy(in = x) } text "input file"
  }
}
