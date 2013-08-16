(ns monotony.test.configured
  (:use monotony.configured
        midje.sweet)
  (:require [monotony.time :as t]
            [monotony.core :as c]))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(facts "about `next-boundary`"
       (fact "generates dates correctly with next-boundary in a with-config block"
             (with-config test-conf
               (next-boundary start-of-2011-gmt :week)) => start-of-second-week-in-2011-gmt))

(facts "about `prior-boundary`"
          (fact "generates dates correctly with prior-boundary in a with-config block"
                (with-config test-conf
                  (prior-boundary a-milli-in-2011 :year)) => start-of-2011-gmt))
