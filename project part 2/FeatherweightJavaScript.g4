grammar FeatherweightJavaScript;


@header { package edu.sjsu.fwjs.parser; }

// Reserved words
IF        	: 'if' ;
ELSE      	: 'else' ;
WHILE		: 'while' ;
FUNCTION	: 'function' ;
VAR			: 'var' ;
PRINT		: 'print' ;

// Literals
INT       	: [1-9][0-9]* | '0' ;
BOOL	  	: 'true' | 'false' ; 
NULL		: 'null' ;
ID			: ['_'][a-zA-Z0-9_]* | [a-zA-Z][a-zA-Z0-9_]* ;

// Symbols
MUL       	: '*' ;
DIV       	: '/' ;
ADD			: '+' ;
SUB			: '-' ;
MOD			: '%' ;
GT			: '>' ;
LT			: '<' ;
GE			: '>=' ;
LE			: '<=' ;
EQ			: '==' ;
SEPARATOR 	: ';'  ;


// Whitespace and comments
NEWLINE   		: '\r'? '\n' -> skip ;
LINE_COMMENT  	: '//' ~[\n\r]* -> skip ;
BLOCK_COMMENT	: '/*' .*? '*/' -> skip ;
WS            	: [ \t]+ -> skip ; // ignore whitespace


// ***Paring rules ***

/** The start rule */
prog: stat+ ;

stat: expr SEPARATOR                                    # bareExpr
    | IF '(' expr ')' block ELSE block                  # ifThenElse
    | IF '(' expr ')' block                             # ifThen
	| WHILE '(' expr ')' block							# while
	| PRINT '(' expr ')' SEPARATOR						# print
	| SEPARATOR 										# empty
    ;

expr: expr op=( '*' | '/' | '%' ) expr                  # MulDivMod
    | expr op=( '+' | '-' ) expr							# AddSub
	| expr op=( '<' | '<=' | '>' | '>=' | '==' ) expr	# Comparison
	| FUNCTION params block				# funcDecl
	| expr args					# funcApp
	| VAR ID '=' expr									# varDecl
	| ID												# varRef
	| ID '=' expr										# varAssign
	| INT                                               # int
	| BOOL												# bool
	| NULL												# null
    | '(' expr ')'                                      # parens
    ;

block: '{' stat* '}'                                    # fullBlock
     | stat                                             # simpBlock
     ;

params: '(' ID* (',' ID)* ')' ;

args: '(' expr* (',' expr)* ')' ;
