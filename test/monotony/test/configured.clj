(ns monotony.test.configured
  (:use monotony.configured
        lazytest.describe)
  (:require [monotony.time :as t]
            [monotony.core :as c]))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(describe next-boundary
          (it "generates dates correctly with next-boundary in a with-config block"
              (= start-of-second-week-in-2011-gmt
                 (with-config test-conf
                   (next-boundary start-of-2011-gmt :week)))))

(describe prior-boundary
          (it "generates dates correctly with prior-boundary in a with-config block"
              (= start-of-2011-gmt
                 (with-config test-conf
                   (prior-boundary a-milli-in-2011 :year)))))
