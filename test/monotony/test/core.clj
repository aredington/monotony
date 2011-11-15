(ns monotony.test.core
  (:use monotony.core
        clojure.test)
  (:require [monotony.time :as t]))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(testing "next-boundary"
  (is (= start-of-second-week-in-2011-gmt (next-boundary test-conf start-of-2011-gmt :week))))

(testing "prior-boundary"
  (is (= start-of-2011-gmt (prior-boundary test-conf a-milli-in-2011 :year))))
