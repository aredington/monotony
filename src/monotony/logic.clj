(ns ^{:doc "Logical assertions and relations for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:use [clojure.math.numeric-tower :only (abs)])
  (:require [clojure.set :as s]
            [clojure.core.logic :as l]
            [clojure.core.logic.arithmetic :as la]
            [monotony.time :as t])
  )

(l/defrel cycle-keyword cycle)
(l/facts cycle-keyword [[:millisecond]
                [:second]
                [:minute]
                [:hour]
                [:day]
                [:week]
                [:month]
                [:year]])

(l/defrel contains-tightly cycle1 number cycle2)
(l/facts contains-tightly [[:second 1000 :millisecond]
                           [:minute 60 :second]
                           [:hour 60 :minute]
                           [:day 24 :hour]
                           [:week 7 :day]])

(l/defrel contains-varying period-name number cycle)
(l/facts contains-varying [[:january 31 :day]
                           [:february 28 :day]
                           [:february-leap 29 :day]
                           [:march 31 :day]
                           [:april 30 :day]
                           [:may 31 :day]
                           [:june 30 :day]
                           [:august 31 :day]
                           [:september 30 :day]
                           [:october 31 :day]
                           [:november 30 :day]
                           [:december 31 :day]
                           [:yearm0 365 :day]
                           [:yearm1 365 :day]
                           [:yearm2 365 :day]
                           [:yearm3 366 :day]])

(l/defrel period-named name named-cycle index containing-cycle)
(l/facts period-named [;; Years of a leap cycle
                       [:yearm0 :year 0 :leap-year-cycle]
                       [:yearm1 :year 1 :leap-year-cycle]
                       [:yearm2 :year 2 :leap-year-cycle]
                       [:yearm3 :year 3 :leap-year-cycle]
                       ;; Months of the year
                       [:january :month 0 :year]
                       [:february :month 1 :year]
                       [:march :month 2 :year]
                       [:april :month 3 :year]
                       [:may :month 4 :year]
                       [:june :month 5 :year]
                       [:july :month 6 :year]
                       [:august :month 7 :year]
                       [:september :month 8 :year]
                       [:october :month 9 :year]
                       [:november :month 10 :year]
                       [:december :month 11 :year]
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

(l/defne smallest-varianto [cycle named-period predicate]
  ([:month :february true] l/s#)
  ([:month _ false] (l/fresh [index]
                             (l/!= named-period :february)
                             (period-named named-period cycle index :year)))
  ([:year :yearm3 false] l/s#)
  ([:year _ true] (l/fresh [index]
                           (l/!= named-period :yearm3)
                           (period-named named-period cycle index :leap-year-cycle))))

;; millis-ino represents a relation between a keyword and the number
;; of millis contained therein, e.g.
;; (millis-ino :millisecond 1)
;; (millis-ino :second 1000)
;; (millis-ino :minute 60000)
(l/defne millis-ino [cycle millis]
  ([:millisecond 1] l/s#)
  ([_ _] (l/fresh [cycle2-count cycle2 cycle2-millis]
                  (l/!= cycle :millisecond)
                  (l/conde
                   ((contains-tightly cycle cycle2-count cycle2))
                   ((l/fresh [named-period]
                             (smallest-varianto cycle named-period true)
                             (contains-varying named-period cycle2-count cycle2))))
                  (millis-ino cycle2 cycle2-millis)
                  (l/project [cycle2-count cycle2-millis]
                             (l/== millis (* cycle2-count cycle2-millis))))))

(l/defne containso [cycle1 cycle2 out]
  ([_ _ true] (l/fresh [millis1 millis2]
                       (millis-ino cycle1 millis1)
                       (millis-ino cycle2 millis2)
                       (la/> millis1 millis2)))
  ([_ _ false] (l/fresh [millis1 millis2]
                        (millis-ino cycle1 millis1)
                        (millis-ino cycle2 millis2)
                        (la/>= millis2 millis1))))

(defn cycle-contains?
  "Compares cycle1 and cycle2 as cycle keywords (e.g. :year, :month),
  returns truthy if cycle1 is a larger cycle of time than cycle2"
  [cycle1 cycle2]
  (first (l/run 1 [q] (containso cycle1 cycle2 q))))

(defn cycles-in
  "Returns a seq of all cycle keywords for cycles that are contained by
  cycle"
  [cycle]
  (set (l/run* [q] (containso cycle q true))))
(alter-var-root #'cycles-in memoize)

(defn cycles-not-in
  "Returns a seq of all cycle keywords for cycles that are not
  contained by cycle"
  [cycle]
  (set (l/run* [q] (containso cycle q false))))
(alter-var-root #'cycles-in memoize)

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
