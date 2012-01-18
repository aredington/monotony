(ns monotony.test.configured
  (:use monotony.configured
        clojure.test)
  (:require [monotony.time :as t]
            [monotony.core :as c]))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(testing "next-boundary"
  (is (= start-of-second-week-in-2011-gmt
         (with-config test-conf
           (next-boundary start-of-2011-gmt :week)))))

(testing "prior-boundary"
  (is (= start-of-2011-gmt
         (with-config test-conf
           (prior-boundary a-milli-in-2011 :year)))))

(testing "periods"
  (is (= 10
         (count (take 10
                      (with-config test-conf
                        (periods :day)))))))
