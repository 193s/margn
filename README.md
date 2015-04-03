# margn
programming language?!

margn -> Java Bytecode


Statements
```
statement ::= let | print
let   ::= "let" id "=" expr
print ::= "print" expr
```

Expressions
```
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


