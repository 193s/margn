name := "margn"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"

libraryDependencies += "org.apache.bcel" % "bcel" % "5.2"


// sbt-assembly

mainClass in assembly := Some("margn.main.Margn")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(Seq("#!/usr/bin/env sh", """exec java -jar "$0" "$@"""" )))

assemblyJarName in assembly := s"${name.value}"

