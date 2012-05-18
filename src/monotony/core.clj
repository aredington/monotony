(ns ^{:doc "A composable take on generating times."
      :author "Alex Redington"}
  monotony.core
  (:require [monotony.time :as t]
            [monotony.constants :as c]
            [monotony.logic :as l])
  (:import java.util.Calendar
           java.util.TimeZone
           java.util.Locale))

(defn new-cal
  "Return a new GMT Calendar instance."
  []
  (Calendar/getInstance (TimeZone/getTimeZone "GMT + 0") Locale/ROOT))

(defn local-cal
  "Return a new local calendar instance."
  []
  (Calendar/getInstance))

(defn new-config
  "Return a new configuration initialized to Midnight, January 1 1970,
and Calendars operating in GMT"
  ([] {:calendar new-cal
       :seed (constantly 0)})
  ([spec] (merge (new-config) spec)))

(defn local-config
  "Returns a new configuration initialized to the system's present
local time and locale."
  ([] {:calendar local-cal
       :seed (fn [] (System/currentTimeMillis))})
  ([spec] (merge (local-config) spec)))

(defn blank-cal
  "Returns a calendar instance initialized to the UNIX epoch."
  [{calendar :calendar}]
  (let [^Calendar blank-cal (calendar)]
    (.setTimeInMillis blank-cal 0)
    blank-cal))

(defn calendar
  "Returns a calendar instance initialized to time"
  [{calendar :calendar} time]
  (let [^Calendar cal (calendar)]
    (do
      (.setTimeInMillis cal (t/millis time))
      cal)))

(defn period-named?
  "Returns true if the period is the named period, false otherwise."
  [config [start end] name]
  (let [[value field] (c/named-periods name)
        ^Calendar start-cal (calendar config start)
        ^Calendar end-cal (calendar config end)]
    (and (= (.get start-cal field) value)
         (= (.get end-cal field) value))))

(defn later
  "Return a time cycle later than seed."
  ([{seed :seed :as config} amount cycle]
     (later config amount cycle (seed)))
  ([config amount cycle seed]
     (let [^Calendar cal (calendar config seed)]
       (do (.add cal (cycle c/cycles) amount)
           (.getTime cal)))))

(defn prior-boundary
  "Returns the start of a bounded cycle including time seed.

  (prior-boundary (now) :year)

  will return 12:00:00AM of the present year."
  [config seed cycle]
  (let [^Calendar cal (calendar config seed)
        cycle-fields (map c/cycles (l/cycles-not-in cycle))
        reset-map (into {} (for [field cycle-fields]
                             [field (.get cal field)]))]
    (.clear cal)
    (doseq [[field value] reset-map]
      (.set cal field value))
    (.getTime cal)))

(defn next-boundary
  "Returns the next clean boundary of a cycle after
  time seed.

  (next-boundary (now) :year)

  will return 12:00:00AM on Jan 1st of the next year."
  [config seed cycle]
  (prior-boundary config (later config 1 cycle seed) cycle))

(defn milli-before
  "Returns the date 1 millisecond before time."
  [time]
  (t/date (dec (t/millis time))))

(defn period-after
  "Returns a period of size corresponding to cycle, starting
  at seed."
  [config seed cycle]
  (vector
   seed
   (milli-before (later config 1 cycle seed))))

(defn period-including
  "Returns a period of size corresponding to cycle, including seed."
  [config seed cycle]
  (vector
   (prior-boundary config seed cycle)
   (next-boundary config seed cycle)))

(defn cycles-in
  "Break a period down by a cycle into multiple sub-periods wholly
  contained in that period. The first sub-period will be inclusive of
  the start of the period. The last sub-period will be exclusive of
  the end of the period. Returns a lazy-seq of the periods."
  [config [start end] cycle]
  (lazy-seq
   (when (< (t/millis start) (t/millis end))
     (cons (period-after config start cycle)
           (cycles-in config [(later config 1 cycle start) end] cycle)))))

(defn bounded-cycles-in
  "Break a period down by a cycle into multiple sub-periods, such that
  the boundaries between periods cleanly map onto calendar breaks,
  e.g. if month is bound to a period of one month

  (bounded-cycles-in month :week)

  Will return a seq of the first partial week of the month, all of the
  weeks starting with Sunday and ending with Saturday, and the last
  partial week of the month"
  [config [start end] cycle]
  (let [first-bounded (next-boundary config start cycle)
        last-bounded (prior-boundary config end cycle)
        start-fragment [start (milli-before first-bounded)]
        cycles-in-period (cycles-in config
                                    [first-bounded
                                     (milli-before last-bounded)] cycle)
        end-fragment [last-bounded end]]
    (concat (list start-fragment)
            cycles-in-period
            (list end-fragment))))

(defn periods
  "Return an lazy infinite sequence of periods with a duration equal to cycle.
  If seed is provided, the first period will include it. Otherwise, period
  will include the result of calling *seed*"
  ([{seed :seed :as config} cycle]
     (periods config cycle (seed)))
  ([config cycle seed]
     (lazy-seq
      (cons (period-after config (prior-boundary config seed cycle) cycle)
            (periods config cycle (later config 1 cycle seed))))))

(defn combine
  "Given one or more seqs of monotonically increasing periods, return
  a lazy seq which interleaves all of them such that:

  the start of period n is greater than the start of period n-1
  the duration of period n is less than the duration of period n-1"

  [& seqs]
  (when-not (every? empty? seqs)
    (let [filled-seqs (filter (comp not empty?) seqs)
          seq-sort-criteria (fn [[[start end]]]
                              [(t/millis start)
                               (- (- (t/millis end) (t/millis start)))])
          seqs-order-by-head (sort-by seq-sort-criteria filled-seqs)
          [[first-period & rest-of-consumed] & unconsumed] seqs-order-by-head]
      (lazy-seq
       (cons first-period
             (apply combine (conj unconsumed rest-of-consumed)))))))

(defn difference
  "Given two or more seqs of monotonically increasing periods, return
  a lazy seq which contains all of the elements of the first seq which
  do not appear in any of the other seqs."
  ([[[first-period-start _ :as first-period] & unconsumed :as all-periods] periods-to-remove]
     (when-not (empty? all-periods)
       (if (empty? periods-to-remove)
         all-periods
         (let [less-than-start? (fn [period]
                                  (< (t/millis (first period))
                                     (t/millis first-period-start)))
               filter-periods (drop-while less-than-start? periods-to-remove)]
           (if (= (first filter-periods) first-period)
             (recur unconsumed
                    filter-periods)
             (lazy-seq
              (cons first-period
                    (difference unconsumed filter-periods))))))))
  ([all-periods first-seq-of-periods-to-remove & seqs-of-periods-to-remove]
     (difference all-periods (apply combine (conj seqs-of-periods-to-remove
                                                  first-seq-of-periods-to-remove)))))

(defn normalize
  "Given a seq of periods and a cycle, returns a lazy seq where each
  element is a period of uniform duration equal to to cycle

  Given a counted seq of periods, return a lazy seq where each element
  is a period of uniform duration, equal to the smallest cycle present
  in the seq. Raises exception if the input seq is not counted?"
  ([config [first & rest :as seq] cycle]
     (when-not (empty? seq)
       (if (= (l/approximate-cycle first)
              cycle)
         (lazy-seq
          (cons first
                (normalize config rest cycle)))
         (lazy-seq
          (concat (bounded-cycles-in config first cycle)
                  (normalize config rest cycle))))))
  ([config seq]
     (when-not (empty? seq)
       (if (counted? seq)
         (normalize config seq (l/min-cycle (map l/approximate-cycle seq)))
         (throw (IllegalArgumentException.
                 "Passed in seq is not counted. Must specify cycle with (normalize config seq cycle)"))))))

(defn contiguous?
  "Returns true if the periods are contiguous, that is, the end of the
  first period occurs immediately before the start of the second
  period, the second period ends immediately before the start of the
  third, etc."
  [& periods]
  (letfn
      [(contiguous-slice? [[period1 period2]]
         (= (- (t/millis (period2 0)) (t/millis (period1 1))) 1))]
    (every? contiguous-slice? (partition 2 1 periods))))

(defn cycles-starting-on
  "Returns the cycle keywords for cycles which can start on the
  provided time, with the largest cycle first."
  [config time]
  (let [periods-including-time (map #(period-including config time %) (keys c/cycles))
        valid-periods (filter #(= (first %) time) periods-including-time)
        sorted-periods (sort-by (fn [[start end]]
                                  (- (t/millis start) (t/millis end))) valid-periods)]
    (map l/approximate-cycle sorted-periods)))

(defn collapse
  "Given a seq of periods, return a lazy seq where each period
  is the largest possible cycle which captures exactly the same span
  of time as the seq."
  ([config seq]
     (collapse config seq #{}))
  ([config [[period-start period-end :as first-period] & rest-periods :as seq] rejected-cycles]
     (when-not (empty? seq)
       (let [[cycle :as potential-cycles] (drop-while
                                           rejected-cycles
                                           (cycles-starting-on config period-start))]
         (when-not (empty? potential-cycles)
           (let [[_ tail :as hypothesis] (period-after config
                                                       period-start
                                                       cycle)
                 [consumed unconsumed] (split-with
                                        #(>= (t/millis tail) (-> % last t/millis))
                                        seq)
                 contiguous? (contiguous? consumed)
                 aligned? (= (last (last consumed)) tail)]
             (if (and contiguous? aligned?)
               (lazy-seq
                (cons hypothesis
                      (collapse config unconsumed #{})))
               (recur config seq (conj rejected-cycles cycle)))))))))
