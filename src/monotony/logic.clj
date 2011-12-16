(ns ^{:doc "Logic assertions for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.arithmetic :as la]))

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

;; millis-ino represents a relation between a keyword and the number
;; of millis contained therein, e.g.
;; (millis-ino :millisecond 1)
;; (millis-ino :second 1000)
;; (millis-ino :minute 60000)
(l/defne millis-ino [cycle millis]
  ([:millisecond 1] (l/succeed l/succeed))
  ([_ _] (l/fresh [cycle2-count cycle2 cycle2-millis]
                  (l/!= cycle :millisecond)
                  (contains-tightly cycle cycle2-count cycle2)
                  (millis-ino cycle2 cycle2-millis)
                  (l/project [cycle2-count cycle2-millis]
                   (l/== millis (* cycle2-count cycle2-millis))))))

(defn contains [cycle1 cycle2]
  (first (l/run 1 [q] (containso cycle1 cycle2 q))))

(defn cycles-in [cycle]
  (l/run* [q] (containso cycle q true)))
