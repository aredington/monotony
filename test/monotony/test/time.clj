(ns monotony.test.time
  (:use monotony.time
        midje.sweet)
  (:require [monotony.core :as m]
            [clj-time.core :as t])
  (:import (java.util Date Calendar TimeZone Locale)))

(def start-of-2011-in-millis 1293840000000)

(def start-of-2011-as-date (Date. start-of-2011-in-millis))

(def one-week-millis (* 1000 60 60 24 7))

(def one-week-GMT [start-of-2011-as-date (Date. (+ start-of-2011-in-millis one-week-millis))])


(facts "monotony.time/Time"
       (facts "extended to Longs"
              (let [subject start-of-2011-in-millis]
                (facts "`time`"
                       (fact "returns the same MS value"
                             (millis subject) => start-of-2011-in-millis))
                (facts "`date`"
                       (fact "returns a GMT date when no config is given"
                             (date subject) => start-of-2011-as-date))
                (facts "`period`"
                       (fact "returns a period with GMT dates when no config is given"
                             (period subject one-week-millis) => one-week-GMT))))
       (facts "extended to Dates"
              (let [subject start-of-2011-as-date]
               (facts "`time`"
                      (fact "returns the corresponding MS value"
                            (millis subject) => start-of-2011-in-millis))
               (facts "`date`"
                      (fact "returns an equivalent date"
                            (date subject) => start-of-2011-as-date))
               (facts "`period`"
                      (fact "returns a period with GMT dates when no config is given"
                            (period subject one-week-millis) => one-week-GMT))))
       (facts "extended to Joda Time instants"
              (let [subject (t/date-time 2011 1 1 0 0 0 0)]
                (facts "`time`"
                       (fact "returns the corresponding MS value"
                             (millis subject) => start-of-2011-in-millis))
                (facts "`date`"
                       (fact "returns an equivalent date"
                             (date subject) => start-of-2011-as-date))
                (facts "`period`"
                       (fact "returns a period with GMT dates when no config is given"
                             (period subject one-week-millis) => one-week-GMT)))))
