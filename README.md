# margn
__Programming language (Compiler)__

margn -> Java Bytecode

## Usage
`sbt run`?!?!


## Specification?

#### Program
```ebnf
program ::= { statement ";" }
```

#### Statements
```ebnf
statement ::= let | print
let   ::= "let" id "=" expr
print ::= "print" expr
```

#### Expressions
```ebnf
expr ::= expr "+" expr
       | expr "-" expr
       | expr "*" expr
       | Expr "/" expr
       | simpleExpr

simpleExpr ::= "-" simpleExpr
             | integerLiteral
             | variable
             | "(" expr ")"

integerLiteral ::= [1-9][0-9]*
                 | 0x[0-9a-fA-F]+
                 | 0b[01]+
```

## TODO
See Issues: https://github.com/193s/margn/issues

