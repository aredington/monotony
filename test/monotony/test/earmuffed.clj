(ns monotony.test.earmuffed
  (:use clojure.test)
  (:require [monotony.earmuffed :as e]
            [monotony.time :as t]
            [monotony.core :as c])
  (:import java.util.Calendar
           java.util.TimeZone
           java.util.Locale))

(alter-var-root #'monotony.earmuffed/*seed* (constantly (constantly 1316802232642)))
(alter-var-root #'monotony.earmuffed/*calendar*
                (constantly (fn []
                              (Calendar/getInstance
                               (TimeZone/getTimeZone "GMT-5")
                               Locale/ROOT))))

(def start-of-2011-est (t/date 1293858000000))

(def start-of-second-week-in-2011-est (t/date 1293944400000))

(def a-milli-in-2011 (t/date 1316802232642))

(testing "next-boundary"
  (is (= start-of-second-week-in-2011-est
         (e/next-boundary start-of-2011-est :week))))

(testing "prior-boundary"
  (is (= start-of-2011-est
         (e/prior-boundary a-milli-in-2011 :year))))
