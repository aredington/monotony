(ns ^{:doc "Logical assertions and relations for reasoning about time."
      :author "Alex Redington"}
  monotony.logic
  (:refer-clojure :exclude [==])
  (:use [clojure.math.numeric-tower :only (abs)])
  (:require [clojure.set :as s]
            [clojure.core.logic :as l]
            [clojure.core.logic.pldb :as pldb]
            [clojure.core.logic.arithmetic :as la]
            [monotony.time :as t]))

(pldb/db-rel cycle-keyword cycle)

(pldb/db-rel contains-tightly cycle1 number cycle2)

(pldb/db-rel contains-varying period-name number cycle)

(pldb/db-rel period-named name named-cycle index containing-cycle)

(def time-db
  (pldb/db
   [cycle-keyword :millisecond]
   [cycle-keyword :second]
   [cycle-keyword :minute]
   [cycle-keyword :hour]
   [cycle-keyword :day]
   [cycle-keyword :week]
   [cycle-keyword :month]
   [cycle-keyword :year]
   [contains-tightly :second 1000 :millisecond]
   [contains-tightly :minute 60 :second]
   [contains-tightly :hour 60 :minute]
   [contains-tightly :day 24 :hour]
   [contains-tightly :week 7 :day]
   [contains-varying :january 31 :day]
   [contains-varying :february 28 :day]
   [contains-varying :february-leap 29 :day]
   [contains-varying :march 31 :day]
   [contains-varying :april 30 :day]
   [contains-varying :may 31 :day]
   [contains-varying :june 30 :day]
   [contains-varying :august 31 :day]
   [contains-varying :september 30 :day]
   [contains-varying :october 31 :day]
   [contains-varying :november 30 :day]
   [contains-varying :december 31 :day]
   [contains-varying :yearm0 365 :day]
   [contains-varying :yearm1 365 :day]
   [contains-varying :yearm2 365 :day]
   [contains-varying :yearm3 366 :day]
   ;; Years of a leap cycle
   [period-named :yearm0 :year 0 :leap-year-cycle]
   [period-named :yearm1 :year 1 :leap-year-cycle]
   [period-named :yearm2 :year 2 :leap-year-cycle]
   [period-named :yearm3 :year 3 :leap-year-cycle]
   ;; Months of the year
   [period-named :january :month 0 :year]
   [period-named :february :month 1 :year]
   [period-named :march :month 2 :year]
   [period-named :april :month 3 :year]
   [period-named :may :month 4 :year]
   [period-named :june :month 5 :year]
   [period-named :july :month 6 :year]
   [period-named :august :month 7 :year]
   [period-named :september :month 8 :year]
   [period-named :october :month 9 :year]
   [period-named :november :month 10 :year]
   [period-named :december :month 11 :year]
   ;; Days of the week
   [period-named :monday :day 0 :week]
   [period-named :tuesday :day 1 :week]
   [period-named :wednesday :day 2 :week]
   [period-named :thursday :day 3 :week]
   [period-named :friday :day 4 :week]
   [period-named :saturday :day 5 :week]
   [period-named :sunday :day 6 :week]
   ;; Hours of the day
   [period-named :12am :hour 0 :day]
   [period-named :midnight :hour 0 :day]
   [period-named :1am :hour 1 :day]
   [period-named :2am :hour 2 :day]
   [period-named :3am :hour 3 :day]
   [period-named :4am :hour 4 :day]
   [period-named :5am :hour 5 :day]
   [period-named :6am :hour 6 :day]
   [period-named :7am :hour 7 :day]
   [period-named :8am :hour 8 :day]
   [period-named :9am :hour 9 :day]
   [period-named :10am :hour 10 :day]
   [period-named :11am :hour 11 :day]
   [period-named :12pm :hour 12 :day]
   [period-named :noon :hour 12 :day]
   [period-named :1pm :hour 1 :day]
   [period-named :2pm :hour 2 :day]
   [period-named :3pm :hour 3 :day]
   [period-named :4pm :hour 4 :day]
   [period-named :5pm :hour 5 :day]
   [period-named :6pm :hour 6 :day]
   [period-named :7pm :hour 7 :day]
   [period-named :8pm :hour 8 :day]
   [period-named :9pm :hour 9 :day]
   [period-named :10pm :hour 10 :day]
   [period-named :11pm :hour 11 :day]))

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
  (pldb/with-db time-db
    (first (l/run 1 [q] (containso cycle1 cycle2 q)))))

(defn cycles-in
  "Returns a seq of all cycle keywords for cycles that are contained by
  cycle"
  [cycle]
  (pldb/with-db time-db
    (set (l/run* [q] (containso cycle q true)))))
(alter-var-root #'cycles-in memoize)

(defn cycles-not-in
  "Returns a seq of all cycle keywords for cycles that are not
  contained by cycle"
  [cycle]
  (pldb/with-db time-db
    (set (l/run* [q] (containso cycle q false)))))
(alter-var-root #'cycles-in memoize)

(defn cycle-for
  "Returns the cycle keyword which generates a seq of periods such
  that each period will be named period-name"
  [period-name]
  (pldb/with-db time-db
    (first (l/run 1 [q]
             (l/fresh [cycle-size index]
               (period-named period-name cycle-size index q))))))

(defn cycle-millis
  "Returns the number of milliseconds for the smallest period
  generated by a cycle keyword."
  [cycle]
  (pldb/with-db time-db
    (first (l/run 1 [q] (millis-ino cycle q)))))
(alter-var-root #'cycle-millis memoize)

(defn- cycle-errors
  [milliseconds]
  (pldb/with-db time-db
    (l/run*
      [q]
      (l/fresh [cycle cycle-millis]
        (millis-ino cycle cycle-millis)
        (l/project [cycle-millis]
                   (l/== q [cycle (- milliseconds cycle-millis)]))))))

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
