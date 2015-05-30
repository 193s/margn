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

![image](https://cloud.githubusercontent.com/assets/6814758/7298230/26448374-ea09-11e4-9cf4-1e1ef13decd1.png)


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
1. `$ git clone https://github.com/193s/margn.git && cd margn`
2. `$ sbt assembly`  
> An executable sh file `./margn` (contains jar) will be generated
3. (Optional) add the current directory to the PATH, or simply copy it to `/usr/local/bin`.

#### Requirements
- Scala
- sbt


## TODO
See Issues: https://github.com/193s/margn/issues

## Specification

### Program
```ebnf
program ::= { statement ";" }
```
`Program` is a sequence of Statements, splitted by ';'.


### Statements
```ebnf
statement ::= let | print | if | assert
```
#### print
```ebnf
print  ::= "print" expr
```
prints \<expr\> with EOL

#### assert
```ebnf
assert ::= "assert" expr
```
evaluates \<expr\> and throws an `AssertionError` if it is `False`.

#### let
```ebnf
let    ::= "let" id "=" expr
```
creates a readonly variable called `id`.

#### if
```ebnf
if     ::= "if" expr ":" statement
```
evaluates \<statement\> if \<expr\> is `True`



### Expressions
```ebnf
expr ::= expr "+" expr
       | expr "-" expr
       | expr "*" expr
       | expr "/" expr
       | expr "==" expr
       | ...
       | simpleExpr

simpleExpr ::= "-" simpleExpr
             | literal
             | variable
             | "(" expr ")"
```

### Types and Literals
#### Integer
```ebnf
integerLiteral ::= [1-9][0-9]*
                 | 0x[0-9a-fA-F]+
                 | 0b[01]+
```


#### String
```ebnf
stringLiteral ::= '"' .* '"'
```


## License
Copyright (c) 2015 193s

Published under the GNU GPLv2, see LICENSE
