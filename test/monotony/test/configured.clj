(ns monotony.test.configured
  (:use monotony.configured
        midje.sweet)
  (:require [monotony.time :as t]
            [monotony.core :as c]))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def first-day-of-2011 (c/period-after test-conf start-of-2011-gmt :day))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(facts "about `period-name`"
       (fact "correctly identifies periods"
             (with-config test-conf
               (period-name first-day-of-2011)) => :saturday))
