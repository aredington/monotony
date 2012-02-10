(ns monotony.test.holidays
  (:use monotony.holidays
        lazytest.describe)
  (:require [monotony.time :as t]
            [monotony.core :as m]))

(def test-conf (m/new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(describe us-holidays
          (let [federal-holidays (us-holidays test-conf)
                weekends (filter #(or (m/period-named? test-conf % :saturday) (m/period-named? test-conf % :sunday)) (m/periods test-conf :day start-of-2011-gmt))
                days (take 365 (m/periods test-conf :day start-of-2011-gmt))
                business-days (m/difference days federal-holidays weekends)]
            (it "generates the correct number of business days"
                (= 252 (count business-days)))))
