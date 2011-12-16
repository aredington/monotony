(ns ^{:doc "Logic assertions for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defrel contains-tightly period1 number period2)
(facts contains-tightly [[:second 1000 :millisecond]
                         [:minute 60 :second]
                         [:hour 60 :minute]
                         [:day 24 :hour]
                         [:week 7 :day]
                         [:non-leap-year 365 :day]
                         [:leap-year 366 :day]])

(defne containso [cycle1 cycle2 out]
  ([_ _ true] (fresh [t] (contains-tightly cycle1 t period2)))
  ([_ _ false] (fresh [t] (contains-tightly cycle2 t period1)))
  ([_ _ _] (fresh [c3]
                  (containso cycle1 c3 out)
                  (containso c3 cycle2 out))))

(defne cycles-for-milliso )

(defn contains [cycle1 cycle2]
  (first (run 1 [q] (containso cycle1 cycle2 q))))

(defn cycles-in [period]
  (run* [q] ()))
