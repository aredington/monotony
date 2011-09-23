(ns monotony.test.core
  (:use monotony.core clojure.test))

(def start-of-2011 (date 1293858000000))

(def start-of-first-week-in-2011 (date 1293944400000))

(def a-milli-on-sep-23rd (date 1316802232642))

(def year-of-2011 (period-after start-of-2011 :year))

(testing "next-boundary"
  (is (= start-of-first-week-in-2011 (next-boundary start-of-2011 :week))))

(testing "prior-boundary"
  (is (= start-of-2011 (prior-boundary a-milli-on-sep-23rd :year))))
