(ns monotony.test.logic
  (:use monotony.logic
        midje.sweet)
  (:require [clojure.core.logic :as l]))

(facts "about `smallest-varianto`"
       (fact "holds for :month :february true"
             (first (l/run 1 [q] (smallest-varianto :month :february q))) => true)
       (fact "holds for :month :march false"
             (first (l/run 1 [q] (smallest-varianto :month :march q))) => false))

(facts "about `millis-ino`"
       (fact "holds for :month and the number of millis in feb"
             (l/run 1 [q] (millis-ino :month (* 1000 60 60 24 28))) => (comp not empty?))
       (fact "holds for :year and the number of millis in a non-leap year"
             (l/run 1 [q] (millis-ino :year (* 1000 60 60 24 365))) => (comp not empty?)))

(facts "about `cycle-contains?`"
       (fact "is true for :week contains :day"
             (cycle-contains? :week :day) => true)
       (fact "is true for :month contains :week"
             (cycle-contains? :month :week) => true)
       (fact "is true for :year contains :month"
             (cycle-contains? :year :month) => true)
       (fact "is false for :year contains :year"
             (cycle-contains? :year :year) => falsey))

(facts "about `cycles-in`"
       (fact "includes all smaller cycles for :day"
             (cycles-in :day) => #{:millisecond :second :minute :hour} )
       (fact "includes all smaller cycless for :month"
             (cycles-in :month) => #{:millisecond :second :minute :hour :day :week})
       (fact "includes all smaller cycles for year"
           (cycles-in :year) => #{:millisecond :second :minute :hour :day :week :month} ))

(facts "about `cycles-not-in`"
       (fact "returns #{:week :month :year} for :day"
             (cycles-not-in :day) => #{:week :month :year :day} ))
