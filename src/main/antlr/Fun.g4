grammar FunParser;


eval returns [double value]
    :    exp=additionExp {$value = $exp.value;}
    ;

additionExp returns [double value]
    :    m1=multiplyExp       {$value =  $m1.value;}
         ( '+' m2=multiplyExp {$value += $m2.value;}
         | '-' m2=multiplyExp {$value -= $m2.value;}
         )*
    ;

multiplyExp returns [double value]
    :    a1=atomExp       {$value =  $a1.value;}
         ( '*' a2=atomExp {$value *= $a2.value;}
         | '/' a2=atomExp {$value /= $a2.value;}
         )*
    ;

atomExp returns [double value]
    :    n=Number                {$value = Double.parseDouble($n.text);}
    |    '(' exp=additionExp ')' {$value = $exp.value;}
    ;




//grammar Exp;
//
//file
//    :   block
//    ;
//
//block
//    : (statement)*
//    ;
//
//blockWithBraces
//    : '{' block '}'
//    ;
//
//Statement
//    : FunctionDeclaration
//    | VariableDeclaration
//    | Expression
//    | whileStatement
//    | ifStatement
//    | Assignment
//    | returnStatement
//    ;
//
//FunctionDeclaration
//    : 'fun' IDENTIFIER '(' ParameterNames ')' BlockWithBraces
//    ;
//
//VariableDeclaration
//    : 'var' IDENTIFIER ('=' expression)?
//    ;
//
//ParameterNames :
//    (IDENTIFIER (',' IDENTIFIER)*)?
//    ;
//
//WhileStatement
//    : 'while' '(' expression ')' BlockWithBraces
//    ;
//
//IfStatement
//    : 'if' '(' expression ')' BlockWithBraces ('else' BlockWithBraces)?
//    ;
//
//Assignment
//    : '=' expression
//    ;
//
//ReturnStatement
//    : 'return' expression
//    ;
//
//Expression
//    : FunctionCall | BinaryExpression | IDENTIFIER | LITERAL | '(' Expression ')'
//    ;
//
//FunctionCall
//    : IDENTIFIER '(' Arguments ')'
//    ;
//
//Arguments
//    : (Expression (',' Expression)*)?
//    ;
//
//BinaryExpression
//    : Addition
//    | Multiplication
//    | Division
//    | Reminder
//    | Greater
//    | Less
//    | GEQ
//    | LEQ
//    | EQ
//    | NEQ
//    | OR
//    | AND
//    ;
//
//Addition
//    : Expression '+' Expression
//    ;
//
//Multiplication
//    : Expression '*' Expression
//    ;
//
//Division
//    : Expression '/' Expression
//    ;
//
//Reminder
//    : Expression '%' Expression
//    ;
//
//Greater
//    : Expression '>' Expression
//    ;
//
//Less
//    : Expression '<' Expression
//    ;
//
//GEQ
//    : Expression '>=' Expression
//    ;
//
//LEQ
//    : Expression '<=' Expression
//    ;
//
//EQ
//    : Expression '==' Expression
//    ;
//
//NEQ
//    : Expression '!=' Expression
//    ;
//
//OR
//    : Expression '||' Expression
//    ;
//
//AND
//    : Expression '&' Expression
//    ;
//
//IDENTIFIER
//    : [a-zA-Z] [a-zA-Z0-9]*
//    ;
//
//COMMENT
//    : '//' ~[\n\r]* -> channel(HIDDEN)
//    ;
//
//
//
//eval returns [double value]
//    :    exp=additionExp {$value = $exp.value;}
//    ;
//
//
//
//additionExp returns [double value]
//    :    m1=multiplyExp       {$value =  $m1.value;}
//         ( '+' m2=multiplyExp {$value += $m2.value;}
//         | '-' m2=multiplyExp {$value -= $m2.value;}
//         )*
//    ;
//
//multiplyExp returns [double value]
//    :    a1=atomExp       {$value =  $a1.value;}
//         ( '*' a2=atomExp {$value *= $a2.value;}
//         | '/' a2=atomExp {$value /= $a2.value;}
//         )*
//    ;
//
//atomExp returns [double value]
//    :    n=Number                {$value = Double.parseDouble($n.text);}
//    |    '(' exp=additionExp ')' {$value = $exp.value;}
//    ;
//
//
//LITERAL
//    :    ('0'..'9')+ ('.' ('0'..'9')+)?
//    ;
//
//WS : (' ' | '\t' | '\r'| '\n') -> skip;