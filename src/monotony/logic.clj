(ns ^{:doc "Logic assertions for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic))

(defrel contains-tightly period1 number period2)
(facts contains-tightly
       [[:second 1000 :millisecond]
        [:minute 60 :second]
        [:hour 60 :minute]
        [:day 24 :hour]
        [:week 7 :day]
        [:non-leap-year 365 :day]
        [:leap-year 366 :day]])

(defne containso [cycle1 cycle2 out]
  ([_ _ true]
     (fresh [t]
       (contains-tightly cycle1 t cycle2)))
  ([_ _ true]
     (fresh [c3 t]
       (contains-tightly cycle1 t c3)
       (containso c3 cycle2 true))))

(defne millis-ino [cycle millis]
  ([:millisecond 1])
  ([_ _]
     (fresh [relation-millis divided-millis cycle2]
       (!= cycle :millisecond)
       (contains-tightly cycle relation-millis cycle2)
       (millis-ino cycle2 divided-millis)
       (project [millis relation-millis divided-millis]
         (== divided-millis
               (/ relation-millis millis))))))

;(defne cycles-for-milliso )

(defn contains [cycle1 cycle2]
  (first (run 1 [q] (containso cycle1 cycle2 q))))

(defn cycles-in [cycle]
  (run* [q] (containso cycle q true)))

(comment
  (run* [q] (millis-ino :millisecond q))
  ;; (1)
  
  (run* [q] (millis-ino :second q))
  ;; blows up with failure to cast the logic var to java.lang.Number,
  ;; despite being in a project. I'm sure I'm doing something wrong
  ;; because I can get projection to work with Ambrose's example at
  ;; http://stackoverflow.com/questions/7668956/arithmetic-and-clojure-functions-on-core-logic-lvars
  )
