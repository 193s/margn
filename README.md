# margn
![GPLv2](https://img.shields.io/badge/license-GPLv2-blue.svg)
[![Build Status](https://travis-ci.org/193s/margn.svg)](https://travis-ci.org/193s/margn)


margn is a JVM-based scripting language that compiles into Java class file.  

```c
let a = 100;
let b = 0xcafe;
print a;
if a == b: print 0;
assert a == 100;
```

## Usage
compile a script:
```sh
$ margn script.mg
```
execute a compiled class file:
```sh
$ java script
```

`margn --help` for more information.

## Install
`$ git clone https://github.com/193s/margn.git && cd margn`, then  
`$ sbt assembly` to generate `./margn` (executable file)

#### Requirements
- Scala
- sbt


## TODO
See Issues: https://github.com/193s/margn/issues

## Specification

#### Program
```ebnf
program ::= { statement ";" }
```

#### Statements
```ebnf
statement ::= let | print | if | assert
if     ::= "if" expr ":" statement
let    ::= "let" id "=" expr
print  ::= "print" expr
assert ::= "assert" expr
```

#### Expressions
```ebnf
expr ::= expr "+" expr
       | expr "-" expr
       | expr "*" expr
       | expr "/" expr
       | expr "==" expr
       | simpleExpr

simpleExpr ::= "-" simpleExpr
             | integerLiteral
             | variable
             | "(" expr ")"

integerLiteral ::= [1-9][0-9]*
                 | 0x[0-9a-fA-F]+
                 | 0b[01]+
```


## License
Copyright (c) 2015 193s

Published under the GNU GPLv2, see LICENSE
