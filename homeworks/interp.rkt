#lang racket

;; Exported methods and structs
(provide evaluate
         sp-val sp-binop sp-if sp-while
         sp-assign sp-var sp-seq)

;; Expressions in the language
(struct sp-val (val))
(struct sp-binop (op exp1 exp2))
(struct sp-if (c thn els))
(struct sp-while (c body))
(struct sp-assign (var exp))
(struct sp-var (varname))
(struct sp-seq (exp1 exp2))

;; Main evaluate method
(define (evaluate prog env)
  (match prog
    [(struct sp-val (v))              (cons v env)] ;; We return a pair of the value and the environment.
    [(struct sp-binop (op exp1 exp2)) (eval-binop op exp1 exp2 env)]
    [(struct sp-if (c thn els))       (eval-if c thn els env)]
    [(struct sp-while (c body))       (eval-while c body env)]
    [(struct sp-assign (var exp))     (eval-assign var exp env)]
    [(struct sp-var (varname))        (cons (hash-ref env varname) env)]
    [(struct sp-seq (exp1 exp2))      (eval-seq exp1 exp2 env)]
    [_                                (error "Unrecognized expression")]))

;; Applies a binary argument to two arguments
(define (eval-binop op e1 e2 env)
  (let* ([r1 (evaluate e1 env)]        ;; Evaluate the lhs expression first
         [v1 (car r1)] [env1 (cdr r1)]
         [r2 (evaluate e2 env1)]       ;; Evaluate the rhs expression second
         [v2 (car r2)] [env2 (cdr r2)])
    (cons (apply op (list v1 v2))      ;; Apply the binary operator to its arguments
          env2)))

;; Evaluates a conditional expression
(define (eval-if c thn els env) 
  {let
      ([condition (evaluate c env)])    ;;get the condition from c
    (if (boolean? (car condition))      ;;make sure it's a boolean
        (if (car condition)
        (evaluate thn env)
        (evaluate els env))
        (error "condition is not a boolean")          ;;the condition was not a boolean
        )
      }
  
) ;; (error "Your code here for ifs"))


;; Evaluates a loop.
;; When the condition is false, return 0.
;; There is nothing special about zero -- we just need to return something.
(define (eval-while c body env)
  {let
      ([envir (evaluate c env)])
       
    (cond
      [(eqv? #f (car envir)) (evaluate (sp-val 0) env)]    ;;if false, the loop is done
      [else (eval-while c body (cdr (evaluate body env)))] ;;recursive call to eval-while to simulate a loop
    )
  } ;;(error "Your code here for loops")
)


;; Handles imperative updates.
(define (eval-assign var exp env)

  {let
      ([envir (hash-set env var (car (evaluate exp env)))])  ;;set up the enviroment for the assign value
    
    (evaluate exp envir)
  }  
)  ;;(error "Your code here for assign"))

;; Handles sequences of statements
(define (eval-seq e1 e2 env)
  {let
      ([first (evaluate e1 env)])    ;;evaluate the first expression
    (evaluate e2 (cdr first))        ;;evaluate the second expression using the first's enviroment
      }
  ;;(error "Your code here for sequence")
)


(define empty-env (hash))

(evaluate (sp-assign "y" (sp-val 18)) (hash "x" 42))
