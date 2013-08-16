(ns monotony.test.core
  (:use monotony.core
        midje.sweet)
  (:require [monotony.time :as t])
  (:import java.util.Locale))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(def start-of-new-years-week-2011 (t/date 1293321600000))

(let [victim-period (period-after test-conf start-of-2011-gmt :day)]
  (facts "about `period-named?`"
         (fact "accepts keywords as the period name"
               (period-named? test-conf victim-period :saturday) => true)
         (fact "accepts strings as the period name"
               (period-named? test-conf victim-period "saturday") => true)
         (fact "throws an exception with a useful error message when the period name is not intelligible to monotony"
               (period-named? test-conf victim-period "foo barington st baz") => (throws IllegalArgumentException #"Unknown period name"))))

(facts "about `next-boundary`"
       (fact "generates the next boundary from a timestamp, config and cycle"
             (next-boundary test-conf start-of-2011-gmt :week) => start-of-second-week-in-2011-gmt))

(facts "about `prior-boundary`"
       (fact "generates the prior boundary from a timestamp, config and cycle"
             (prior-boundary test-conf a-milli-in-2011 :year) => start-of-2011-gmt )
       (fact "returns its argument for the week including new years 2011"
             (prior-boundary test-conf start-of-new-years-week-2011 :week) => start-of-new-years-week-2011 ))

(facts "about `combine`"
       (let [thirty-mondays (take 30 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
             thirty-tuesdays (take 30 (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt)))
             combination (combine thirty-mondays thirty-tuesdays)]
         (facts "for finite seqs"
                (fact "combines two seqs"
                      (count combination) => 60)
                (fact "combines the seqs in order, mondays are the odds"
                      (partition 1 2 combination) => (partial every? (comp #(period-named? test-conf % :monday) first)))
                (fact "combines the seqs in order, tuesdays are the evens"
                      (partition 1 2 (rest combination)) => (partial every? (comp #(period-named? test-conf % :tuesday) first)))))

       (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
             tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
             combination (take 60 (combine mondays tuesdays))]
         (facts "for infinite seqs"
                (fact "combines infinite seqs"
                      (count combination) => 60)
                (fact "combines the seqs in order, mondays are the odds"
                      (partition 1 2 combination) => (partial every? (comp #(period-named? test-conf % :monday) first)))
                (fact "combines the seqs in order, tuesdays are the evens"
                      (partition 1 2 (rest combination)) => (partial every? (comp #(period-named? test-conf % :tuesday) first)))))

       (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
             tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
             wednesdays (filter #(period-named? test-conf % :wednesday) (periods test-conf :day start-of-2011-gmt))
             combination (take 60 (combine mondays tuesdays wednesdays))]
         (facts "for more than two seqs"
                (fact "combines the three seqs"
                      (count combination) => 60)
                (fact "combines the seqs in order, mondays are modulo 3 + 0"
                      (partition 1 3 combination) => (partial every? (comp #(period-named? test-conf % :monday) first)))
                (fact "combines the seqs in order, tuesdays are modulo 3 + 1"
                      (partition 1 3 (rest combination)) => (partial every? (comp #(period-named? test-conf % :tuesday) first)))
                (fact "combines the seqs in order, wednesdays are modulo 3 + 2"
                      (partition 1 3 (drop 2 combination)) => (partial every? (comp #(period-named? test-conf % :wednesday) first) )))))

(facts "about `difference`"
       (let [fifty-two-mondays (take 52 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
             days-of-2011 (take 365 (periods test-conf :day start-of-2011-gmt))
             minus-mondays (difference days-of-2011 fifty-two-mondays)]
         (facts "for finite seqs"
                (fact "generates the difference of two seqs"
                      (count minus-mondays) => 313)
                (fact "didn't include any of the removed periods"
                      minus-mondays => (partial every? (comp not #(period-named? test-conf % :monday))))))

       (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
             days-of-2011 (periods test-conf :day start-of-2011-gmt)
             minus-mondays (take 313 (difference days-of-2011 mondays))]
         (facts "for infinite seqs"
                (fact "generates the difference of two seqs"
                      (count minus-mondays) => 313)
                (fact "didn't include any of the removed periods"
                      minus-mondays => (partial every? (comp not #(period-named? test-conf % :monday))))))

       (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
             tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
             combo (combine mondays tuesdays)
             days-of-2011 (periods test-conf :day start-of-2011-gmt)
             minus-mondays-and-tuesdays (take 261 (difference days-of-2011 mondays tuesdays))]
         (facts "subtracting two infinite seqs"
                (fact "removes the 2nd through nth seqs from the first seq"
                      (count minus-mondays-and-tuesdays) => 261)
                (fact "removed all the mondays"
                      minus-mondays-and-tuesdays => (partial every? (comp not #(period-named? test-conf % :monday))))
                (fact "removed all the tuesdays"
                      minus-mondays-and-tuesdays => (partial every? (comp not #(period-named? test-conf % :tuesday)))))))

(facts  "about `normalize`"
        (let [eight-days (take 8 (periods test-conf :day start-of-2011-gmt))
              three-weeks (take 3
                                (periods
                                 test-conf
                                 :week
                                 (next-boundary test-conf (last (last eight-days)) :week)))
              last-days-of-jan (take 2
                                     (periods
                                      test-conf
                                      :day
                                      (next-boundary test-conf (last (last three-weeks)) :day)))
              three-months (take 3
                                 (periods
                                  test-conf
                                  :month
                                  (next-boundary test-conf (last (last three-weeks)) :month)))
              a-maze-of-twisty-periods (concat eight-days three-weeks last-days-of-jan three-months)
              normalized (normalize test-conf a-maze-of-twisty-periods :day)]
          (facts "with a heterogeneous input seq"
                 (fact "generates a seq of day long periods"
                       (count normalized) => (+ 31 28 31 30))
                 (fact "generates a seq of periods of uniform length"
                       (every? #(= 86399999 (- (t/millis (% 1)) (t/millis (% 0)))) normalized) => true)
                 (fact "throws an exception if handed a seq which is not counted? and no cycle keyword"
                       (normalize test-conf a-maze-of-twisty-periods) => (throws IllegalArgumentException "Passed in seq is not counted. Must specify cycle with (normalize config seq cycle)"))
                 (fact "doesn't need the explicit cycle keyword duration if handed a counted? seq"
                       (count (normalize test-conf (vec a-maze-of-twisty-periods))) => (+ 31 28 31 30) )
                 (fact "generates a seq of periods of uniform length without a cycle keyword"
                       (normalize test-conf (vec a-maze-of-twisty-periods)) => (partial every? #(= 86399999 (- (t/millis (% 1)) (t/millis (% 0)))))))))

(facts "about `contiguous?`"
          (fact "returns true for two contiguous vectors"
              (contiguous? [1 2] [3 4]) => true)
          (fact "returns false for two non-contiguous vectors"
              (contiguous? [1 2] [4 5]) => falsey)
          (fact "returns true for 3 contiguous vectors"
              (contiguous? [1 2] [3 4] [5 6]) => true)
          (fact "returns false for 3 non-contiguous vectors"
              (contiguous? [1 2] [3 4] [6 7]) => falsey)
          (fact "returns true for periods generated by monotony that are contiguous"
              (apply contiguous? (take 3 (periods test-conf :day))) => true)
          (fact "returns false for periods generated by monotony that are not contiguous"
              (apply contiguous? (map first (partition 1 2 (take 10 (periods test-conf :day))))) => falsey))

(facts "about `cycles-starting-on`"
       (fact "includes year first for the new year"
             (first (cycles-starting-on test-conf start-of-2011-gmt)) => :year)
       (fact "includes week first for the week including the new year"
             (first (cycles-starting-on test-conf (first (period-including test-conf start-of-2011-gmt :week)))) => :week))

(facts "about `collapse`"
       (let [first-week (first (periods test-conf :week start-of-2011-gmt))
             first-weeks-days (bounded-cycles-in test-conf first-week :day)]
         (fact "collapses 7 days in one week to that week"
               (collapse test-conf first-weeks-days) => (list first-week)))
       (let [days-from-2011-on (periods test-conf :day start-of-2011-gmt)
             months-from-2011-on (periods test-conf :month start-of-2011-gmt)
             january-days (take 31 days-from-2011-on)
             january-month (take 1 months-from-2011-on)
             seven-feb-days (take 7 (drop 31 days-from-2011-on))
             march-days (take 31 (drop 28 (drop 31 days-from-2011-on)))
             march-month (take 1 (drop 2 months-from-2011-on))
             some-days (concat january-days seven-feb-days march-days)
             collapsed-periods (concat january-month seven-feb-days march-month)]
         (fact "collapses march and january 2011 leaving february days intact"
               (collapse test-conf some-days) => collapsed-periods)))
