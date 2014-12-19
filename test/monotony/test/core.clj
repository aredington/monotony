(ns monotony.test.core
  (:use monotony.core
        clojure.test)
  (:require [monotony.time :as t])
  (:import java.util.Locale))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(def start-of-new-years-week-2011 (t/date 1293321600000))


(deftest period-named-test
  (testing "`period-named?`"
    (let [victim-period (period-after test-conf start-of-2011-gmt :day)]
      (testing "accepts keywords as the period name"
        (is (= (period-named? test-conf victim-period :saturday)  true)))
      (testing "accepts strings as the period name"
        (is (period-named? test-conf victim-period "saturday")))
      (testing "throws an exception with a useful error message when the period name is not intelligible to monotony"
        (is (thrown-with-msg? IllegalArgumentException #"Unknown period name" (period-named? test-conf victim-period "foo barington st baz")))))))

(deftest period-name-test
  (testing "`period-name"
    (let [day-period (period-after test-conf start-of-2011-gmt :day)
          hour-period (period-after test-conf start-of-2011-gmt :hour)
          month-period (period-after test-conf start-of-2011-gmt :month)
          year-period (period-after test-conf start-of-2011-gmt :year)]
      (testing "names days"
        (period-name test-conf day-period)  :saturday)
      (testing "names midnight"
        (period-name test-conf hour-period)  :midnight)
      (testing "names months"
        (period-name test-conf month-period)  :january)
      (testing "names years with an integer"
        (period-name test-conf year-period)  2011))))

(deftest next-boundary-test
  (testing "`next-boundary`"
    (testing "generates the next boundary from a timestamp, config and cycle"
      (next-boundary test-conf start-of-2011-gmt :week)  start-of-second-week-in-2011-gmt)))

(deftest prior-boundary-test
  (testing "`prior-boundary`"
    (testing "generates the prior boundary from a timestamp, config and cycle"
      (is (= (prior-boundary test-conf a-milli-in-2011 :year) start-of-2011-gmt)))
    (testing "returns its argument for the week including new years 2011"
      (is (= (prior-boundary test-conf start-of-new-years-week-2011 :week) start-of-new-years-week-2011)))))

(deftest combine-test
  (testing "`combine`"
    (let [thirty-mondays (take 30 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
          thirty-tuesdays (take 30 (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt)))
          combination (combine thirty-mondays thirty-tuesdays)]
      (testing "finite seqs"
        (testing "combines two seqs"
          (is (= (count combination) 60)))
        (testing "combines the seqs in order, mondays are the odds"
          (is (every? (comp #(period-named? test-conf % :monday) first) (partition 1 2 combination))))
        (testing "combines the seqs in order, tuesdays are the evens"
          (is (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 2 (rest combination)))))))

    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
          combination (take 60 (combine mondays tuesdays))]
      (testing "infinite seqs"
        (testing "combines infinite seqs"
          (is (= (count combination) 60)))
        (testing "combines the seqs in order, mondays are the odds"
          (is (every? (comp #(period-named? test-conf % :monday) first) (partition 1 2 combination))))
        (testing "combines the seqs in order, tuesdays are the evens"
          (is (every? (comp #(period-named? test-conf % :tuesday) first)
                      (partition 1 2 (rest combination)))))))

    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
          wednesdays (filter #(period-named? test-conf % :wednesday) (periods test-conf :day start-of-2011-gmt))
          combination (take 60 (combine mondays tuesdays wednesdays))]
      (testing "for more than two seqs"
        (testing "combines the three seqs"
          (is (= (count combination) 60)))
        (testing "combines the seqs in order, mondays are modulo 3 + 0"
          (is (every? (comp #(period-named? test-conf % :monday) first) (partition 1 3 combination))))
        (testing "combines the seqs in order, tuesdays are modulo 3 + 1"
          (is (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 3 (rest combination)))))
        (testing "combines the seqs in order, wednesdays are modulo 3 + 2"
          (is (every? (comp #(period-named? test-conf % :wednesday) first) (partition 1 3 (drop 2 combination)))))))))

(deftest difference-test
  (testing "`difference`"
    (let [fifty-two-mondays (take 52 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
          days-of-2011 (take 365 (periods test-conf :day start-of-2011-gmt))
          minus-mondays (difference days-of-2011 fifty-two-mondays)]
      (testing "for finite seqs"
        (testing "generates the difference of two seqs"
          (is (= (count minus-mondays) 313)))
        (testing "didn't include any of the removed periods"
          (is (every? (comp not #(period-named? test-conf % :monday)) minus-mondays)))))

    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          days-of-2011 (periods test-conf :day start-of-2011-gmt)
          minus-mondays (take 313 (difference days-of-2011 mondays))]
      (testing "for infinite seqs"
        (testing "generates the difference of two seqs"
          (is (= (count minus-mondays) 313)))
        (testing "didn't include any of the removed periods"
          (is (every? (comp not #(period-named? test-conf % :monday)) minus-mondays)))))

    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
          combo (combine mondays tuesdays)
          days-of-2011 (periods test-conf :day start-of-2011-gmt)
          minus-mondays-and-tuesdays (take 261 (difference days-of-2011 mondays tuesdays))]
      (testing "subtracting two infinite seqs"
        (testing "removes the 2nd through nth seqs from the first seq"
          (is (= (count minus-mondays-and-tuesdays)) 261))
        (testing "removed all the mondays"
          (is (every? (comp not #(period-named? test-conf % :monday)) minus-mondays-and-tuesdays)))
        (testing "removed all the tuesdays"
          (is (every? (comp not #(period-named? test-conf % :tuesday)) minus-mondays-and-tuesdays)))))))

(deftest normalize-test
  (testing "`normalize`"
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
      (testing "with a heterogeneous input seq"
        (testing "generates a seq of day long periods"
          (is (= (count normalized)) (+ 31 28 31 30)))
        (testing "generates a seq of periods of uniform length"
          (is (every? #(= 86399999 (- (t/millis (% 1)) (t/millis (% 0)))) normalized)))
        (testing "throws an exception if handed a seq which is not counted? and no cycle keyword"
          (is (thrown-with-msg? IllegalArgumentException #"Passed in seq is not counted\. Must specify cycle with \(normalize config seq cycle\)" (normalize test-conf a-maze-of-twisty-periods))))
        (testing "doesn't need the explicit cycle keyword duration if handed a counted? seq"
          (is (= (count (normalize test-conf (vec a-maze-of-twisty-periods))) (+ 31 28 31 30))))
        (testing "generates a seq of periods of uniform length without a cycle keyword"
          (is (every? #(= 86399999 (- (t/millis (% 1)) (t/millis (% 0)))) (normalize test-conf (vec a-maze-of-twisty-periods)))))))))

(deftest contiguous-test
  (testing "`contiguous?`"
    (testing "returns true for two contiguous vectors"
      (is (contiguous? [1 2] [3 4])))
    (testing "returns false for two non-contiguous vectors"
      (is (not (contiguous? [1 2] [4 5]))))
    (testing "returns true for 3 contiguous vectors"
      (is (contiguous? [1 2] [3 4] [5 6])))
    (testing "returns false for 3 non-contiguous vectors"
      (is (not (contiguous? [1 2] [3 4] [6 7]))))
    (testing "returns true for periods generated by monotony that are contiguous"
      (is (apply contiguous? (take 3 (periods test-conf :day)))))
    (testing "returns false for periods generated by monotony that are not contiguous"
      (is (not (apply contiguous? (map first (partition 1 2 (take 10 (periods test-conf :day))))))))))

(deftest cycles-starting-on-test
  (testing "`cycles-starting-on`"
    (testing "includes year first for the new year"
      (is (= (first (cycles-starting-on test-conf start-of-2011-gmt)) :year)))
    (testing "includes week first for the week including the new year"
      (is (= (first (cycles-starting-on test-conf (first (period-including test-conf start-of-2011-gmt :week)))) :week)))))

(deftest collapse-test
  (testing "`collapse`"
    (let [first-week (first (periods test-conf :week start-of-2011-gmt))
          first-weeks-days (bounded-cycles-in test-conf first-week :day)]
      (testing "collapses 7 days in one week to that week"
        (is (= (collapse test-conf first-weeks-days) (list first-week)))))
    (let [days-from-2011-on (periods test-conf :day start-of-2011-gmt)
          months-from-2011-on (periods test-conf :month start-of-2011-gmt)
          january-days (take 31 days-from-2011-on)
          january-month (take 1 months-from-2011-on)
          seven-feb-days (take 7 (drop 31 days-from-2011-on))
          march-days (take 31 (drop 28 (drop 31 days-from-2011-on)))
          march-month (take 1 (drop 2 months-from-2011-on))
          some-days (concat january-days seven-feb-days march-days)
          collapsed-periods (concat january-month seven-feb-days march-month)]
      (testing "collapses march and january 2011 leaving february days intact"
        (is (= (collapse test-conf some-days) collapsed-periods))))))
