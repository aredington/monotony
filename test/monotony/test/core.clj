(ns monotony.test.core
  (:use monotony.core
        lazytest.describe
        lazytest.expect.thrown)
  (:require [monotony.time :as t])
  (:import java.util.Locale))

(def test-conf (new-config))

(def start-of-2011-gmt (t/date 1293840000000))

(def start-of-second-week-in-2011-gmt (t/date 1293926400000))

(def a-milli-in-2011 (t/date 1316802232642))

(describe next-boundary
          (it "generates the next boundary from a timestamp, config and cycle"
              (= start-of-second-week-in-2011-gmt (next-boundary test-conf start-of-2011-gmt :week))))

(describe prior-boundary
          (it "generates the prior boundary from a timestamp, config and cycle"
              (= start-of-2011-gmt (prior-boundary test-conf a-milli-in-2011 :year))))

(describe combine
  (testing "for finite seqs"
    (let [thirty-mondays (take 30 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
          thirty-tuesdays (take 30 (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt)))
          combination (combine thirty-mondays thirty-tuesdays)]
      (it "combines two seqs"
       (= 60 (count combination)))
      (it "combines the seqs in order, mondays are the odds"
       (every? (comp #(period-named? test-conf % :monday) first) (partition 1 2 combination)))
      (it "combines the seqs in order, tuesdays are the evens"
       (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 2 (rest combination))))))

  (testing "for infinite seqs"
    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
          combination (take 60 (combine mondays tuesdays))]
      (it "combines infinite seqs"
       (= 60 (count combination)))
      (it "combines the seqs in order, mondays are the odds"
       (every? (comp #(period-named? test-conf % :monday) first) (partition 1 2 combination)))
      (it "combines the seqs in order, tuesdays are the evens"
       (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 2 (rest combination))))))

  (testing "for more than two seqs"
    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
          wednesdays (filter #(period-named? test-conf % :wednesday) (periods test-conf :day start-of-2011-gmt))
          combination (take 60 (combine mondays tuesdays wednesdays))]
      (it "combines the three seqs"
       (= 60 (count combination)))
      (it "combines the seqs in order, mondays are modulo 3 + 0"
       (every? (comp #(period-named? test-conf % :monday) first) (partition 1 3 combination)))
      (it "combines the seqs in order, tuesdays are modulo 3 + 1"
       (every? (comp #(period-named? test-conf % :tuesday) first) (partition 1 3 (rest combination))))
      (it "combines the seqs in order, wednesdays are modulo 3 + 2"
       (every? (comp #(period-named? test-conf % :wednesday) first) (partition 1 3 (drop 2 combination)))))))

(describe difference
  (testing "for finite seqs"
    (let [fifty-two-mondays (take 52 (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt)))
          days-of-2011 (take 365 (periods test-conf :day start-of-2011-gmt))
          minus-mondays (difference days-of-2011 fifty-two-mondays)]
      (it "generates the difference of two seqs"
          (= 313 (count minus-mondays)))
      (it "didn't include any of the removed periods"
       (every? (comp not #(period-named? test-conf % :monday)) minus-mondays))))

  (testing "for infinite seqs"
    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          days-of-2011 (periods test-conf :day start-of-2011-gmt)
          minus-mondays (take 313 (difference days-of-2011 mondays))]
      (it "generates the difference of two seqs"
       (= 313 (count minus-mondays)))
      (it "didn't include any of the removed periods"
       (every? (comp not #(period-named? test-conf % :monday)) minus-mondays))))

  (testing "subtracting two infinite seqs"
    (let [mondays (filter #(period-named? test-conf % :monday) (periods test-conf :day start-of-2011-gmt))
          tuesdays (filter #(period-named? test-conf % :tuesday) (periods test-conf :day start-of-2011-gmt))
          combo (combine mondays tuesdays)
          days-of-2011 (periods test-conf :day start-of-2011-gmt)
          minus-mondays-and-tuesdays (take 261 (difference days-of-2011 mondays tuesdays))]
      (it "removes the 2nd through nth seqs from the first seq"
          (= 261 (count minus-mondays-and-tuesdays)))
      (it "removed all the mondays"
          (every? (comp not #(period-named? test-conf % :monday)) minus-mondays-and-tuesdays))
      (it "removed all the tuesdays"
          (every? (comp not #(period-named? test-conf % :tuesday)) minus-mondays-and-tuesdays)))))

(describe normalize
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
    (it "generates a seq of day long periods"
     (= (+ 31 28 31 30) (count normalized)))
    (it "generates a seq of periods of uniform length"
     (every? #(= 86399999 (- (t/millis (% 1)) (t/millis (% 0)))) normalized))
    (it "throws an exception if handed a seq which is not counted? and no cycle keyword"
     (throws? IllegalArgumentException (normalize test-conf a-maze-of-twisty-periods)))
    (it "doesn't need the explicit cycle keyword duration if handed a counted? seq"
     (= (+ 31 28 31 30) (count (normalize test-conf (vec a-maze-of-twisty-periods)))))
    (it "generates a seq of periods of uniform length without a cycle keyword"
     (every? #(= 86399999 (- (t/millis (% 1)) (t/millis (% 0)))) (normalize test-conf (vec a-maze-of-twisty-periods))))))

(describe contiguous?
  (it "returns true for two contiguous vectors"
   (contiguous? [1 2] [3 4]))
  (it "returns false for two non-contiguous vectors"
   (not (contiguous? [1 2] [4 5])))
  (it "returns true for 3 contiguous vectors"
      (contiguous? [1 2] [3 4] [5 6]))
  (it "returns false for 3 non-contiguous vectors"
      (not (contiguous? [1 2] [3 4] [6 7])))
  (it "returns true for periods generated by monotony that are contiguous"
      (apply contiguous? (take 3 (periods test-conf :day))))
  (it "returns false for periods generated by monotony that are not contiguous"
      (not (apply contiguous? (map first (partition 1 2 (take 10 (periods test-conf :day))))))))
