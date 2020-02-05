#lang racket

;; The big-num data structure is essentially a list of 3 digit numbers.

;; Exporting methods
(provide big-add big-subtract big-multiply big-power-of pretty-print number->bignum string->bignum)

(define MAX_BLOCK 1000)

;; Addition of two big-nums
(define (big-add x y)
  (big-add1 x y 0)
  )

;;helper function to add two number blocks (0-999)
(define (helper-add x y co)
  {let ([sum (+ x y co)])
    ;;999+999=1998, so carry out will be no bigger than 1.
    (if (> sum 999)
        (append (list 1) (list (- sum 1000)))
        (append (list sum))
        )
    }
)

;;are we appending something or nah...?
(define (big-add1 x y co)
  (append (cond
    ;; If both lists are empty, the return value is either 0 or the caryover value.
    [(and (= 0 (length x)) (= 0 (length y)))
      (if (= co 0) '() (list co))]
    ;;handles the cases if one of the list no longer has a block of numbers
    [(= 0 (length x))  (append (big-add1 (list co) y 0))]
    [(= 0 (length y))  (append (big-add1 x (list co) 0))]
    [else
       ;;
       ;; --- YOUR CODE HERE ---
       ;;
     {let
         ([result (helper-add (car x) (car y) co)])
     (if (= 2 (length result))
            (begin
              (append (cdr result)
              (big-add1 (list-tail x 1) (list-tail y 1) (car result))))
            (begin
              (append result
              (big-add1 (list-tail x 1) (list-tail y 1) 0)))
            )
     }
     ;;(error "Not implemented")
     ]
    ))
)

;; Subtraction of two big-nums
(define (big-subtract x y)
  (let ([lst (big-subtract1 x y 0)])
    (reverse (strip-leading-zeroes (reverse lst)))
  ))

(define (strip-leading-zeroes x)
  (cond
    [(= 0 (length x)) '(0)]
    [(= 0 (car x)) (strip-leading-zeroes (cdr x))]
    [else x]
    ))

;;subtracts two blocks in two big-nums
(define (helper-sub bnt bnb)
  {let
      ([diff (- (car bnt) (car bnb))])                   ;;if the stars align, do it like kindergarten 
    (if (<= 0 diff)
        (list diff)
        (append (list (-(car (cdr bnt)) 1)) (list (+ 1000 diff)))  ;;append the block after borrowing from it and the difference
        )
    }
  )


;; NOTE: there are no negative numbers with this implementation,
;; so 3 - 4 should throw an error.
;; x is the top y is the bottom
;; My implementation doesn't use the "borrow" parameter 
(define (big-subtract1 x y borrow)
  ;;
  ;; --- YOUR CODE HERE ---
  ;;
  (cond
    [(and (= 0 (length x)) (= 0 (length y))) (append '())]
    [(= 0 (length y)) (append (list (car x)) (big-subtract1 (cdr x) y 0))]
    [(equal? (length x) (length y))
     (if (< (list-ref x (- (length x) 1)) (list-ref y (- (length y) 1)))
         (error "difference will be a negative number")
         {let
         ([diff (helper-sub x y)])
         (if (= 2 (length diff))
             (begin
             (append (cdr diff)
             (big-subtract1 (append (list (car diff)) (cddr x)) (cdr y) 0)))
             (begin
             (append diff
             (big-subtract1 (cdr x) (cdr y) 0)))
             )
         }
         )]
    [(< (length x) (length y)) (error "difference will be a negative number")]
    [else
     {let
         ([diff (helper-sub x y)])
         (if (= 2 (length diff))
             (begin
             (append (cdr diff)
             (big-subtract1 (append (list (car diff)) (cddr x)) (cdr y) 0)))
             (begin
             (append diff
             (big-subtract1 (cdr x) (cdr y) 0)))
             )
         }]
      )
)

;; Returns true if two big-nums are equal
(define (big-eq x y)
  ;;
  ;; --- YOUR CODE HERE ---
  ;;
  (cond
    [(and (= 0 (length x)) (= 0 (length y)))]
    [(eqv? (car x) (car y)) (big-eq (cdr x) (cdr y))]
    [else #f]
    )
  
  ;;(error "Not implemented"))
)
;; Decrements a big-num
(define (big-dec x)
  (big-subtract x '(1))
  )


;;indents a list by putting a block of 0's at the head of the list
(define (indenter i f lst)

  {let
      ([nlst (append '(0) lst)])
  (cond
    [(= 0 f) lst]
    [(= f i) nlst]
    [else (append (indenter (+ i 1) f nlst))]
    )
    }
  )


;;multiplies a big-num by a integer block (0-999)
;;returns big-num in "backwards" order so that it's easier to feed into big-add
(define (helper-multi bn i co)
  {let
      ([result (+ co (* (car bn) i))]
       
       )
    (cond
      [(= 1 (length bn))
           (if (> result 999)
        (append (list (- result (* 1000 (floor (/ result 1000))))) (list (floor (/ result 1000))))
        (append (list result))
        )]
      ;[(> result 999) (append (list (floor (/ result 1000))) (list (- result (* 1000 (floor (/ result 1000)))) ))]
     ; []
      [else (if (> result 999)
        (append (list (- result (* 1000 (floor (/ result 1000))))) (helper-multi (cdr bn) i (floor (/ result 1000))))
        (append (list result) (helper-multi (cdr bn) i (floor (/ result 1000))))
        ) ];(helper-multi (cdr bn) i (floor (/ result 1000)))]
      )
   }
)

(define (big-multi1 bnt bnb pp ic)
  
  {let
      ([product (big-add pp (indenter 1 ic (helper-multi bnt (car bnb) 0)))])
    (indenter 1 ic pp)
    (cond
      [(= 1 (length bnb)) product]
      [else (big-multi1 bnt (cdr bnb) product (+ ic 1))]
      )

  }
)

;; Multiplies two big-nums
(define (big-multiply x y)
  ;;
  ;; --- YOUR CODE HERE ---
  ;;
  
  ;; Follow the same approach that you learned in
  ;; grade school for multiplying numbers, except
  ;; that a "block" is 0-999, instead of 0-9.
  ;; Consider creating a helper function that multiplies
  ;; a big-number with a integer in the range of 0-999.
  ;; Once you have that working, you can use it in your
  ;; solution here.

  (cond
    [(or (= 0 (length x)) (= 0 (length y))) (error "empty list detected")]
    [(or (equal? '(0) x) (equal? '(0) y)) (list 0)]
    [else
     (cond
       [(< (length x) (length y)) (big-multi1 y x '(0) 0)]
       [(> (length x) (length y)) (big-multi1 x y '(0) 0)]
       [else (big-multi1 x y '(0) 0)])])
    
  
  ;;(error "Not implemented")
)
(define (power-helper i sum x y)
  
  {let ([s (big-multiply sum x)])   
                   
  (if (= i y)
      s
      (power-helper (+ i 1) s x y)
  )   
  }
)

;; Raise x to the power of y
(define (big-power-of x y)
  ;;
  ;; --- YOUR CODE HERE ---
  ;;
  
  ;;(* x x)
  (cond [(= 0 (car y)) 1]
        [(= 1 (car y)) x]
        [else (car (power-helper 2 x x (car y)))]
  )
  ;; Solve this function in terms of big-multiply. 
  ;(error "Not implemented")
)

;; Dispaly a big-num in an easy to read format
(define (pretty-print x)
  (let ([lst (reverse x)])
    (string-append
     (number->string (car lst))
     (pretty-print1 (cdr lst))
     )))

(define (pretty-print1 x)
  (cond
    [(= 0 (length x))  ""]
    [else
     (string-append (pretty-print-block (car x)) (pretty-print1 (cdr x)))]
    ))

(define (pretty-print-block x)
  (string-append
   ","
   (cond
     [(< x 10) "00"]
     [(< x 100) "0"]
     [else ""])
   (number->string x)))

;; Convert a number to a bignum
(define (number->bignum n)
  (cond
    [(< n MAX_BLOCK) (list n)]
    [else
     (let ([block (modulo n MAX_BLOCK)]
           [rest (floor (/ n MAX_BLOCK))])
       (cons block (number->bignum rest)))]))

;; Convert a string to a bignum
(define (string->bignum s)
  (let ([n (string->number s)])
    (number->bignum n)))



(big-subtract '(999 999) '(999 999))

