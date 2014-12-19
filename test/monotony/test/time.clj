(ns monotony.test.time
  (:use monotony.time
        clojure.test)
  (:require [monotony.core :as m]
            [clj-time.core :as t])
  (:import (java.util Date Calendar TimeZone Locale)))

(def start-of-2011-in-millis 1293840000000)

(def start-of-2011-as-date (Date. start-of-2011-in-millis))

(def one-week-millis (* 1000 60 60 24 7))

(def one-week-GMT [start-of-2011-as-date (Date. (+ start-of-2011-in-millis one-week-millis))])


(deftest Time-test
  (testing "monotony.time/Time"
    (testing "extended to Longs"
      (let [subject start-of-2011-in-millis]
        (testing "`time`"
          (testing "returns the same MS value"
            (is (= (millis subject) start-of-2011-in-millis))))
        (testing "`date`"
          (testing "returns a GMT date when no config is given"
            (is (= (date subject) start-of-2011-as-date))))
        (testing "`period`"
          (testing "returns a period with GMT dates when no config is given"
            (is (= (period subject one-week-millis) one-week-GMT))))))
    (testing "extended to Dates"
      (let [subject start-of-2011-as-date]
        (testing "`time`"
          (testing "returns the corresponding MS value"
            (is (= (millis subject) start-of-2011-in-millis))))
        (testing "`date`"
          (testing "returns an equivalent date"
            (is (= (date subject) start-of-2011-as-date))))
        (testing "`period`"
          (testing "returns a period with GMT dates when no config is given"
            (is (= (period subject one-week-millis) one-week-GMT))))))
    (testing "extended to Joda Time instants"
      (let [subject (t/date-time 2011 1 1 0 0 0 0)]
        (testing "`time`"
          (testing "returns the corresponding MS value"
            (is (= (millis subject) start-of-2011-in-millis))))
        (testing "`date`"
          (testing "returns an equivalent date"
            (is (= (date subject) start-of-2011-as-date))))
        (testing "`period`"
          (testing "returns a period with GMT dates when no config is given"
            (is (= (period subject one-week-millis) one-week-GMT))))))))
