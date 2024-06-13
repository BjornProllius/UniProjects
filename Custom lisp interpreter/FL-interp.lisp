#|
Bjorn Prollius
CMPUT 325 Lec B1
Assignment 2
|#


; This function is used to get a replacement for an element from the new list.
; It takes three arguments: 'element' (the element to be replaced), 'old' (the old list), and 'new' (the new list).
(defun get-replacement (element old new)
    ; If old is empty, return nil.
    (if (null old) nil
        ; If element is equal to the first element of old, return the first element of new.
        (if (eq element (car old))
            (car new)
            ; otherwise, call get-replacement recursively with the rest of old and new.
            (get-replacement element (cdr old) (cdr new)))))


; This function substitutes elements in L that match elements in old with matching elements from new.
(defun substitute-exp (old new L)
    (if (null L) nil
        (cons (substitute-helper old new (car L)) 
              (substitute-exp old new (cdr L)))))


; This function helps substitute-exp by handling the substitution of individual elements.
; This function takes three arguments: 'old'(old list), 'new' (new list), and 'element'(current element to be replaced).
(defun substitute-helper (old new element)
    ;find a replacement for element in the new list
    (let ((replacement-value (get-replacement element old new)))
        ;if a replacement was found, return it
        (cond ((not (null replacement-value)) replacement-value)
              ;return element if is an atom
              ((atom element) element)
               ;otherwise, call substitute-exp recursively because element is a list
              (t (substitute-exp old new element)))))



;This function checks if x is in L
(defun is-member (x L)
  (cond ((null L) nil)
        ((equal x (car L)) t)
        (t (is-member x (cdr L)))))



;This function checks if x is in L, and if x is a list, it searches in the list
(defun is-member-enhanced (x L)
    (cond ((null L) nil)  ; If L is empty, return nil.
                ((equal x (car L)) t)  ; If x is equal to the first element of L, return t.
                ((is-list-strict (car L)) (or (is-member-enhanced x (car L)) (is-member-enhanced x (cdr L))))  ; If the first element of L is a list, search both in it and in the rest of L.
                (t (is-member-enhanced x (cdr L)))))  ; Otherwise, search in the rest of L.



;This function counts the number of arguments in arg
(defun count-args (arg)
    (if (null arg)
            0
            (+ 1 (count-args (cdr arg)))))



;This function checks that x is both an atom and not a number
(defun not-number (x)
    (and (atom x) (not (numberp x))))

;this function checks if x is a list or an atom
(defun is-list-or-atom (x)
    (or (null x) (and (not (atom x)) (is-list-or-atom (cdr x)))))


;this function checks if x is a proper list
(defun is-list-strict (x)
    (and (not (atom x)) (or (null x) (is-list-or-atom (cdr x)))))


; This function handles primitive operations.
; It takes three arguments: f (function anme), arg (the arguments to the function), and P (the rest of the program).
(defun handle-primitive (f arg P)
    (cond
    ;if is dealt with first because it is not applicative order
    ((eq f 'if) (if (fl-interp (car arg) P) (fl-interp (cadr arg) P) (fl-interp (caddr arg) P))) 
    (t
    ;evaluate arguments first
    (let* ((eval-arg (mapcar #'(lambda (x) (fl-interp x P)) arg))) 
        (cond 
            ((eq f 'car)  (car (car  eval-arg)))
            ((eq f 'cdr)  (cdr (car eval-arg)))
            ((eq f 'cons) (cons (car eval-arg) (cadr eval-arg))) 
            ((eq f 'eq)   (if (eq (car eval-arg) (cadr eval-arg)) T NIL)) 
            ((eq f 'atom) (if (atom (car eval-arg)) T NIL)) 
            ((eq f 'null) (if (null (car eval-arg)) T NIL))
            ((eq f '+)    (+ (car eval-arg) (cadr eval-arg))) 
            ((eq f '-)    (- (car eval-arg) (cadr eval-arg))) 
            ((eq f '*)    (* (car eval-arg) (cadr eval-arg))) 
            ((eq f '>)    (if (> (car eval-arg) (cadr eval-arg)) T NIL)) 
            ((eq f '<)    (if (< (car eval-arg) (cadr eval-arg)) T NIL)) 
            ((eq f '=)    (if (= (car eval-arg) (cadr eval-arg)) T NIL)) 
            ((eq f 'and)  (if (and (car eval-arg) (cadr eval-arg)) T NIL)) 
            ((eq f 'or)   (if (or (car eval-arg) (cadr eval-arg)) T NIL)) 
            ((eq f 'not)  (if (not (car eval-arg)) T NIL)) 
            ((eq f 'number) (if (numberp (car eval-arg)) T NIL)) 
            ((eq f 'equal) (if (equal (car eval-arg) (cadr eval-arg)) T NIL)))))))


; This function finds a function definition in a program.
; It takes three arguments: f (the function name), n (the number of arguments), and P (the program).
(defun find-def (f n P) 
    (cond
        ;if P is empty, return nil. 
        ((null P) nil) 
        ; If the name of the first function in P matches f and the number of arguments matches n, return the function definition.
        ((and (eq (caar P) f) (= (count-args (cadar P)) n)) (car P)) 
        ;check the rest of the program
        (t (find-def f n (cdr P))))) 


; This function handles user-defined functions.
; It takes three arguments: f (the function name), arg (the arguments to the function), and P (the rest of the program).
(defun handle-user-defined (f arg P)
    ; Find the definition of the function f in the program P.
    (let* ((def (find-def f (count-args arg) P))
            ; get the arguments and the body from the function definition.
           (def-args (cadr def))
           (def-body (cadddr def))
           ; Evaluate the arguments to the function.
           (eval-arg (mapcar #'(lambda (x) (if (is-list-or-atom x) x (fl-interp x P))) arg))) 
        ; Substitute the evaluated arguments into the function body and interpret the result.
        (fl-interp (substitute-exp def-args eval-arg def-body) P)))

;This function handles (some) lambda functions
;It takes one argument: E (the expression to be evaluated)
(defun handle-lambda (E)
    (if (is-list-strict E)
            ; Evaluate the first element of E
            (let ((function (eval (car E)))) ;evaluate the first element of the expression and bind it to function
            ; Recursively evaluate the rest of the expression
            (let ((args (mapcar #'handle-lambda (cdr E)))) ;evaluate the rest of the expression and bind it to args
            ; Apply the function to the arguments
            (apply function args))) ;apply function (evaluation of lambda function) to args (evaluation of the rest of the expression)
        E))



(defun fl-interp (E P)
    ;; Define a list of primitive functions.
    (let ((primitive-functions '(if car cdr cons eq atom null + - * > < = and or not number equal)))
        (cond 
            ;; If E is an atom, return E.
            ((atom E) E)
            (t
            ;; extract the function name f and arguments arg from E.
            (let ((f (car E)) (arg (cdr E)))
                (cond 
                    ;; If f is a lambda function, handle it with handle-lambda.
                    ((is-member-enhanced 'lambda E) (handle-lambda E)) ;cannot mix lambda and other types of functions
                    ;; If f is a primitive function and f does not contain 'lambda', handle it with handle-primitive.
                    ((and (not-number f) (is-member f primitive-functions)) (handle-primitive f arg P)) 
                    ;; If f is a user-defined function and f does not contain 'lambda', handle it with handle-user-defined.
                    ((and (not-number f) (not (is-member f primitive-functions))) (handle-user-defined f arg P))                    
                    (t E)))))))
