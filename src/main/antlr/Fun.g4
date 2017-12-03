grammar Fun;

/* parser rules */

file
    : block
    ;

block
    : (statement)*
    ;

blockWithBraces
    : LeftBrace block RightBrace
    ;

statement
    : functionDeclaration
    | variableDeclaration
    | whileStatement
    | ifStatement
    | assignment
    | returnStatement
    | expression
    ;

functionDeclaration
    : Fun Identifier LeftParenthese parameterNames RightParenthese blockWithBraces
    ;

variableDeclaration
    : Var Identifier (Assign expression)?
    ;

parameterNames :
    (Identifier (Comma Identifier)*)?
    ;

whileStatement
    : While LeftParenthese expression RightParenthese blockWithBraces
    ;

ifStatement
    : If LeftParenthese expression RightParenthese blockWithBraces (Else blockWithBraces)?
    ;

assignment
    : Identifier Assign expression
    ;

returnStatement
    : Return expression
    ;

expression
    : Identifier LeftParenthese arguments RightParenthese # functionCall
    | expression MultiplicationOperations expression # multExpression
    | expression AdditionOperations expression # addExpression
    | expression RelationOperations expression # relExpression
    | expression EqualityOperations expression # eqExpression
    | expression And expression # andExpression
    | expression Or expression # orExpression
    | Number # literal
    | Identifier # variableIdentifier
    | LeftParenthese expression RightParenthese # atomicExpression
    ;

arguments
    : (expression (Comma expression)*)?
    ;

/* lexer rules */

/* keywords */

Fun : 'fun' ;
Var : 'var' ;
While : 'while' ;
If : 'if' ;
Else : 'else' ;
Return : 'return' ;
Assign : '=' ;

Identifier : [a-zA-Z] [a-zA-Z0-9]* ;
Number : ('-')? ('0' | ('1'..'9') + ('0'..'9')*) ;
Comment : '//' ~[\n\r]* -> channel(HIDDEN) ;
WS : [ \t\r\n] -> skip;
LeftBrace : '{' ;
RightBrace : '}' ;
LeftParenthese : '(' ;
RightParenthese : ')' ;
Comma : ',' ;


/* binary operations */

AdditionOperations : '+' | '-' ;
MultiplicationOperations : '*' | '/' | '%' ;
RelationOperations : '>' | '<' | '>=' | '<=' ;
EqualityOperations : '==' | '!=' ;
And : '&&' ;
Or : '||' ;