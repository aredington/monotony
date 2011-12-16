(ns ^{:doc "Logic assertions for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :as l]))

(l/defrel contains-tightly period1 number period2)
(l/facts contains-tightly [[:second 1000 :millisecond]
                           [:minute 60 :second]
                           [:hour 60 :minute]
                           [:day 24 :hour]
                           [:week 7 :day]
                           [:non-leap-year 365 :day]
                           [:leap-year 366 :day]])

(l/defne containso [cycle1 cycle2 out]
  ([_ _ true] (l/fresh [t] (contains-tightly cycle1 t cycle2)))
  ([_ _ true] (l/fresh [c3 t]
                  (contains-tightly cycle1 t c3)
                  (containso c3 cycle2 true))))

(l/defne millis-ino [cycle millis]
  ([:millisecond 1] (l/succeed l/succeed))
  ([_ _] (l/fresh [relation-millis divided-millis cycle2]
                (contains-tightly cycle relation-millis cycle2)
                (millis-ino cycle2 divided-millis)
                (l/project [millis relation-millis divided-millis]
                           (l/== divided-millis
                                 (/ relation-millis millis))))))

;(defne cycles-for-milliso )

(defn contains [cycle1 cycle2]
  (first (l/run 1 [q] (containso cycle1 cycle2 q))))

(defn cycles-in [cycle]
  (l/run* [q] (containso cycle q true)))

(comment
  (require ['monotony.logic :as 'ml]
           ['clojure.core.logic :as 'l])
  (l/run* [q] (ml/millis-ino :millisecond q))
  ;; (1)
  (l/run* [q] (ml/millis-ino :second q))
  ;; blows up with failure to cast the logic var to java.lang.Number,
  ;; despite being in a project. I'm sure I'm doing something wrong
  ;; because I can get projection to work with Ambrose's example at
  ;; http://stackoverflow.com/questions/7668956/arithmetic-and-clojure-functions-on-core-logic-lvars
  )
