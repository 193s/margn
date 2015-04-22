name := "margn"

version := "1.0"

scalaVersion := "2.11.5"

// scala parser combinator
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"

// apache bcel
libraryDependencies += "org.apache.bcel" % "bcel" % "5.2"

// scopt
libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"


// sbt-assembly

mainClass in assembly := Some("margn.main.Margn")

assemblyOutputPath in assembly := file(s"./${name.value}/")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(Seq("#!/usr/bin/env sh", """exec java -jar "$0" "$@"""" )))

assemblyJarName in assembly := s"${name.value}"

