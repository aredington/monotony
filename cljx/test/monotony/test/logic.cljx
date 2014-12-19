(ns monotony.test.logic
  (:use monotony.logic
        clojure.test)
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.pldb :as pldb]))

(deftest smallest-varianto-test
  (pldb/with-db time-db
    (testing "`smallest-varianto`"
      (testing "holds for :month :february true"
        (is (first (l/run 1 [q] (smallest-varianto :month :february q)))))
      (testing "holds for :month :march false"
        (is (not (first (l/run 1 [q] (smallest-varianto :month :march q)))))))))

(deftest millis-ino-test
  (pldb/with-db time-db
    (testing "`millis-ino`"
      (testing "holds for :month and the number of millis in feb"
        (is (not (empty? (l/run 1 [q] (millis-ino :month (* 1000 60 60 24 28)))))))
      (testing "holds for :year and the number of millis in a non-leap year"
        (is (not (empty? (l/run 1 [q] (millis-ino :year (* 1000 60 60 24 365))))))))))

(deftest cycle-contains?-test
  (testing "`cycle-contains?`"
    (testing "is true for :week contains :day"
      (is (cycle-contains? :week :day)))
    (testing "is true for :month contains :week"
      (is (cycle-contains? :month :week)))
    (testing "is true for :year contains :month"
      (is (cycle-contains? :year :month)))
    (testing "is false for :year contains :year"
      (is (not (cycle-contains? :year :year))))))

(deftest cycles-in-test
  (testing "`cycles-in`"
    (testing "includes all smaller cycles for :day"
      (is (= (cycles-in :day)) #{:millisecond :second :minute :hour}))
    (testing "includes all smaller cycless for :month"
      (is (= (cycles-in :month) #{:millisecond :second :minute :hour :day :week})))
    (testing "includes all smaller cycles for year"
      (is (= (cycles-in :year) #{:millisecond :second :minute :hour :day :week :month})) )))

(deftest cycles-not-in-test
  (testing "`cycles-not-in`"
    (testing "returns #{:week :month :year} for :day"
      (is (= (cycles-not-in :day) #{:week :month :year :day})) )))
