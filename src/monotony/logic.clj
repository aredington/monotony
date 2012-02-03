(ns ^{:doc "Logical assertions and relations for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:use [clojure.math.numeric-tower :only (abs)])
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.arithmetic :as la]
            [monotony.time :as t])
  )

(l/defrel contains-tightly period1 number period2)
(l/facts contains-tightly [[:second 1000 :millisecond]
                           [:minute 60 :second]
                           [:hour 60 :minute]
                           [:day 24 :hour]
                           [:week 7 :day]
                           [:year 365 :day]
                           [:year 12 :month]
                           [:leap-year 366 :day]])

(l/defrel period-named name named-period index containing-period)
(l/facts period-named [
                       ;; Days of the week
                       [:monday :day 0 :week]
                       [:tuesday :day 1 :week]
                       [:wednesday :day 2 :week]
                       [:thursday :day 3 :week]
                       [:friday :day 4 :week]
                       [:saturday :day 5 :week]
                       [:sunday :day 6 :week]
                       ;; Hours of the day
                       [:12am :hour 0 :day]
                       [:midnight :hour 0 :day]
                       [:1am :hour 1 :day]
                       [:2am :hour 2 :day]
                       [:3am :hour 3 :day]
                       [:4am :hour 4 :day]
                       [:5am :hour 5 :day]
                       [:6am :hour 6 :day]
                       [:7am :hour 7 :day]
                       [:8am :hour 8 :day]
                       [:9am :hour 9 :day]
                       [:10am :hour 10 :day]
                       [:11am :hour 11 :day]
                       [:12pm :hour 12 :day]
                       [:noon :hour 12 :day]
                       [:1pm :hour 1 :day]
                       [:2pm :hour 2 :day]
                       [:3pm :hour 3 :day]
                       [:4pm :hour 4 :day]
                       [:5pm :hour 5 :day]
                       [:6pm :hour 6 :day]
                       [:7pm :hour 7 :day]
                       [:8pm :hour 8 :day]
                       [:9pm :hour 9 :day]
                       [:10pm :hour 10 :day]
                       [:11pm :hour 11 :day]
                       ])

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

(l/defne containso [cycle1 cycle2 out]
  ([_ _ true] (l/fresh [millis1 millis2]
                       (millis-ino cycle1 millis1)
                       (millis-ino cycle2 millis2)
                       (la/> millis1 millis2))))

(defn cycle-contains?
  "Compares cycle1 and cycle2 as cycle keywords (e.g. :year, :month),
  returns truthy if the cycle1 is a larger cycle of time than cycle2"
  [cycle1 cycle2]
  (first (l/run 1 [q] (containso cycle1 cycle2 q))))

(defn cycles-in
  "Returns a seq of all cycle keywords for cycles that are contained by
  cycle"
  [cycle]
  (l/run* [q] (containso cycle q true)))

(defn cycle-for
  "Returns the cycle keyword which generates a seq of periods such
  that each period will be named period-name"
  [period-name]
  (first (l/run 1 [q]
                (l/fresh [cycle-size index]
                         (period-named period-name cycle-size index q)))))

(defn- cycle-errors
  [milliseconds]
  (l/run*
   [q]
   (l/fresh [cycle cycle-millis]
            (millis-ino cycle cycle-millis)
            (l/project [cycle-millis]
                       (l/== q [cycle (- milliseconds cycle-millis)])))))

(defn approximate-cycle
  "Returns the cycle keyword which best fits the period passed in"
  [period]
  (let [start-millis (t/millis (period 0))
        end-millis (t/millis (period 1))
        contained-millis (- end-millis start-millis)
        cycle-errors (cycle-errors contained-millis)]
    (ffirst (sort-by (comp abs second) cycle-errors))))

(defn min-cycle
  "Return the smallest cycle keyword contained in a seq of cycle keywords"
  [cycles]
  (let [cset (set cycles)]
    (first
     (filter #(not-any? (partial cycle-contains? %) (disj cset %)) cset))))
