(ns monotony.test.logic
  (:use monotony.logic
        lazytest.describe)
  (:require [clojure.core.logic :as l]))

(describe smallest-varianto
          (it "holds for :month :february true"
              (first (l/run 1 [q] (smallest-varianto :month :february q))))
          (it "holds for :month :march false"
              (not (first (l/run 1 [q] (smallest-varianto :month :march q))))))

(describe millis-ino
          (it "holds for :month and the number of millis in feb"
              (not (empty? (l/run 1 [q] (millis-ino :month (* 1000 60 60 24 28))))))
          (it "holds for :year and the number of millis in a non-leap year"
              (not (empty? (l/run 1 [q] (millis-ino :year (* 1000 60 60 24 365)))))))

(describe cycle-contains?
          (it "returns true for :week contains :day"
              (cycle-contains? :week :day))
          (it "returns true for :month contains :week"
              (cycle-contains? :month :week))
          (it "returns true for :year contains :month"
              (cycle-contains? :year :month))
          (it "returns false for :year contains :year"
              (not (cycle-contains? :year :year))))

(describe cycles-in
          (it "calculates correctly for day"
           (= #{:millisecond :second :minute :hour} (cycles-in :day)))
          (it "calculates correctly for month"
           (= #{:millisecond :second :minute :hour :day :week} (cycles-in :month)))
          (it "calculates correctly for year"
              (= #{:millisecond :second :minute :hour :day :week :month} (cycles-in :year))))

(describe cycles-not-in
          (it "returns #{:week :month :year} for :day"
              (= #{:week :month :year :day} (cycles-not-in :day))))
