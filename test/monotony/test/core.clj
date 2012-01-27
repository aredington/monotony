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

(testing "combine for finite seqs"
  (let [thirty-mondays (take 30 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
        thirty-tuesdays (take 30 (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt)))
        combination (combine thirty-mondays thirty-tuesdays)]
    (is (= 60 (count combination)))
    (is (every? (comp #(period-named? test-conf % :monday) first) (partition 1 2 combination)))
    (is (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 2 (rest combination))))))

(testing "combine for infinite seqs"
  (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
        tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
        combination (take 60 (combine mondays tuesdays))]
    (is (= 60 (count combination)))
    (is (every? (comp #(period-named? test-conf % :monday) first) (partition 1 2 combination)))
    (is (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 2 (rest combination))))))

(testing "combine for more than two seqs"
  (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
        tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
        wednesdays (filter #(period-named? test-conf % :wednesday) (periods test-conf :day start-of-2011-gmt))
        combination (take 60 (combine mondays tuesdays wednesdays))]
    (is (= 60 (count combination)))
    (is (every? (comp #(period-named? test-conf % :monday) first) (partition 1 3 combination)))
    (is (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 3 (rest combination))))
    (is (every? (comp #(period-named? test-conf % :wednesday) first) (partition 1 3 (drop 2 combination))))))

(testing "difference for finite seqs"
  (let [fifty-two-mondays (take 52 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
        days-of-2011 (take 365 (periods test-conf :day start-of-2011-gmt))
        minus-mondays (difference days-of-2011 fifty-two-mondays)]
    (is (= 313 (count minus-mondays)))
    (is (every? (comp not #(period-named? test-conf % :monday)) minus-mondays))))

(testing "difference for infinite seqs"
  (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
        days-of-2011 (periods test-conf :day start-of-2011-gmt)
        minus-mondays (take 313 (difference days-of-2011 mondays))]
    (is (= 313 (count minus-mondays)))
    (is (every? (comp not #(period-named? test-conf % :monday)) minus-mondays))))

(testing "difference, subtracting two infinite seqs"
  (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
        tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
        combo (combine mondays tuesdays)
        days-of-2011 (periods test-conf :day start-of-2011-gmt)
        minus-mondays-and-tuesdays (take 261 (difference days-of-2011 mondays tuesdays))]
    (is (= 261 (count minus-mondays-and-tuesdays)))
    (is (every? (comp not #(period-named? test-conf % :monday)) minus-mondays-and-tuesdays))
    (is (every? (comp not #(period-named? test-conf % :tuesday)) minus-mondays-and-tuesdays))))
