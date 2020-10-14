# margn
![GPLv2](https://img.shields.io/badge/license-GPLv2-blue.svg)
[![Build Status](https://travis-ci.org/193s/margn.svg)](https://travis-ci.org/193s/margn)


margn is a JVM-based scripting language that compiles your source code into a Java class file.  

```c
let a = 100;
print "a == 100:";

if a == 100 : {
  print "yes";
  print "a =";
  print a;
};
else        : print "no";
```

![ss 2015-05-30 at 22 01 27](https://cloud.githubusercontent.com/assets/6814758/7897456/7c107b3e-0717-11e5-969f-68480924d97f.png)


## Usage
To compile a script, type:
```sh
$ margn script.mg
```
To execute a compiled class file, type:
```sh
$ java script
```

Type `margn --help` for more detailed information.

## Installation
1. `$ git clone https://github.com/193s/margn.git && cd margn`
2. `$ sbt assembly`  
> An executable shell file `./margn` (contains jar) will be generated
3. (Optional) add your current directory to the PATH, or simply move the file to `/usr/local/bin`.

#### Requirements
- Scala
- sbt


## TODO
See Issues: https://github.com/193s/margn/issues

## Language Reference

### Program
```ebnf
program ::= { statement ";" }
```
`Program` is a sequence of Statements, splitted by ';'.


### Statements
```ebnf
statement ::= block | print | assert | let | if | pass
```
#### print
```ebnf
print  ::= "print" expr
```
prints \<expr\> with EOL

#### pass
```ebnf
pass ::= "pass"
```
null operation

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

#### block
```ebnf
block ::= "{" program "}"
```
block statements

#### if
```ebnf
if     ::= "if" expr ":" statement
```
evaluates \<statement\> if \<expr\> is `True`



### Expressions
```ebnf
expr ::= expr op expr
       | "-" simpleExpr
       | literal
       | variable
       | "(" expr ")"
```

#### Operators
##### Unary Operators
```ebnf
unary_op ::=  ("-"|"+"|"~"|"!") expr
```

##### Binary Operators
```ebnf
bi_op ::= expr op expr
op    ::= "+" | "-" | "==" | ...
```

| Precedence | Op            | Description                 |
|:-----------|:--------------|-----------------------------|
| 0          | and or        | logical and/or              |
| 0          | ^             | xor                         |
| 1          | == !=         | compare                     |
|            | > >= < <=     |                             |
| 2          | + -           | addition and subtraction    |
| 3          | * /           | multiplication and division |


### Types and Literals
#### Int
```ebnf
integerLiteral ::= [1-9][0-9]*
                 | 0x[0-9a-fA-F]+
                 | 0b[01]+
```
32-bit integer (range: `-2147483648` ~ `2147483647`)

#### Bool
```ebnf
booleanLiteral ::= "true" | "false"
```
Logical type

#### String
```ebnf
stringLiteral ::= '"' .* '"'
```


## License
Copyright (c) 2015 193s

Published under the GNU GPLv2, see LICENSE
