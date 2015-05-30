# margn
![GPLv2](https://img.shields.io/badge/license-GPLv2-blue.svg)
[![Build Status](https://travis-ci.org/193s/margn.svg)](https://travis-ci.org/193s/margn)


margn is a JVM-based scripting language that compiles into Java class file.  

```c
print "hello!";
print "10 + 20 * 3 = ";
let a = 10 + 20*3;
print a;
assert a == 70;
```

![ss 2015-05-30 at 22 01 27](https://cloud.githubusercontent.com/assets/6814758/7897456/7c107b3e-0717-11e5-969f-68480924d97f.png)


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

## Installation
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
#### Int (Integer)
```ebnf
integerLiteral ::= [1-9][0-9]*
                 | 0x[0-9a-fA-F]+
                 | 0b[01]+
```

#### Bool (Boolean)
```ebnf
booleanLiteral ::= "true" | "false"
```

#### String
```ebnf
stringLiteral ::= '"' .* '"'
```


## License
Copyright (c) 2015 193s

Published under the GNU GPLv2, see LICENSE
