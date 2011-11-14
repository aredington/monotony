(ns ^{:doc "A composable take on generating times."
      :author "Alex Redington"}
  monotony.core
  (:require [monotony.time :as t]
            [monotony.constants :as c])
  (:import java.util.Date
           java.util.Calendar
           java.util.TimeZone
           java.util.Locale))

(defn new-cal
  "Return a new default calendar instance."
  []
  (Calendar/getInstance (TimeZone/getTimeZone "GMT + 0") Locale/ROOT))

(defn new-config
  ([] {:calendar new-cal
       :seed (constantly 0)})
  ([spec] (merge (new-config) spec)))

(defn now
  "Returns the current time"
  []
  (Date.))

(defn ^{:configured true} blank-cal
  "Returns a calendar instance initialized to the UNIX epoch."
  [config]
  (let [^Calendar blank-cal ((:calendar config))]
    (.setTimeInMillis blank-cal 0)
    blank-cal))

(defn ^{:configured true} calendar
  "Returns a calendar instance initialized to time"
  [config time]
  (let [^Calendar cal ((:calendar config))]
    (do
      (.setTimeInMillis cal (t/millis time))
      cal)))

(defn ^{:configured true} period-named?
  "Returns true if the period is the named period, false otherwise."
  [config period name]
  (let [named-period (c/named-periods name)
        ^Calendar start-cal (calendar config (period 0))
        ^Calendar end-cal (calendar config (period 1))]
    (and (= (.get start-cal (named-period 1)) (named-period 0))
         (= (.get end-cal (named-period 1)) (named-period 0)))))

(defn ^{:configured true} later
  "Return a time cycle later than seed."
  ([config amount cycle]
     (later config amount cycle ((:seed config))))
  ([config amount cycle seed]
     (let [^Calendar cal (calendar config seed)]
       (do (.add cal (cycle c/cycles) amount)
           (.getTime cal)))))

(defn ^{:configured true} contained-cycle-keywords
  "Return a list of all the cycle keywords contained by keyword.
  :year contains :month, :week, :day, :hour, :minute, and :second"
  [config keyword]
  (if (contains? c/cycle-keywords keyword)
    (c/cycle-keywords keyword)
    (let [after-epoch (fn [kw]
                        (let [^Calendar blank-cal (blank-cal config)]
                          (.add blank-cal (kw c/cycles) 1)
                          (.getTimeInMillis blank-cal)))]
      (map #(get c/cycles %)
           (filter #(< (after-epoch %)
                       (after-epoch keyword)) (keys c/cycles))))))

(defn ^{:configured true} prior-boundary
  "Returns the start of a bounded cycle including time seed.

  (prior-boundary (now) :year)

  will return 12:00:00AM of the present year."
  [config seed cycle]
  (let [^Calendar cal (calendar config seed)
        cycle-vals (reverse (contained-cycle-keywords config cycle))]
    (doseq [contained-cycle-val cycle-vals]
      (.set cal contained-cycle-val
            (.getActualMinimum cal contained-cycle-val)))
    (.getTime cal)))

(defn ^{:configured true} next-boundary
  "Returns the next clean boundary of a cycle after
  time seed.

  (next-boundary (now) :year)

  will return 12:00:00AM on Jan 1st of the next year."
  [config seed cycle]
  (prior-boundary config (later 1 cycle seed) cycle))

(defn milli-before
  "Returns the date 1 millisecond before time."
  [time]
  (t/date (- (t/millis time) 1)))

(defn ^{:configured true} period-after
  "Returns a period of size equal to cycle, starting
  at seed."
  [config seed cycle]
  (vector
   seed
   (milli-before (later config 1 cycle seed))))

(defn ^{:configured true} cycles-in
  "Break a period down by a cycle into multiple sub-periods wholly
  contained in that period. The first sub-period will be inclusive of
  the start of the period. The last sub-period will be exclusive of
  the end of the period. Returns a lazy-seq of the periods."
  [config period cycle]
  (let [start (period 0)
        end (period 1)]
    (lazy-seq
     (when (< (t/millis start) (t/millis end))
       (cons (period-after config start cycle)
             (cycles-in config [(later 1 cycle start) end] cycle))))))

(defn ^{:configured true} bounded-cycles-in
  "Break a period down by a cycle into multiple sub-periods, such that
  the boundaries between periods cleanly map onto calendar breaks,
  e.g. if month is bound to a period of one month

  (bounded-cycles-in month :week)

  Will return a seq of the first partial week of the month, all of the
  weeks starting with Sunday and ending with Saturday, and the last
  partial week of the month"
  [config period cycle]
  (let [start (period 0)
        end (period 1)
        first-bounded (next-boundary config start cycle)
        last-bounded (prior-boundary config end cycle)
        start-fragment [start (milli-before first-bounded)]
        cycles-in-period (cycles-in config
                                    [first-bounded
                                     (milli-before last-bounded)] cycle)
        end-fragment [last-bounded end]]
    (concat (list start-fragment)
            cycles-in-period
            (list end-fragment))))

(defn ^{:configured true} periods
  "Return an lazy infinite sequence of periods with a duration equal to cycle.
  If seed is provided, the first period will include it. Otherwise, period
  will include the result of calling *seed*"
  ([config cycle]
     (periods config cycle ((:seed config))))
  ([config cycle seed]
     (lazy-seq
      (cons (period-after config (prior-boundary config seed cycle) cycle)
            (periods config cycle (later config 1 cycle seed))))))

(defn combine
  "Given one or more seqs of periods, return a lazy seq which
  interleaves all of them such that:

  the start of seq n is greater than the start of seq n-1
  the duration of seq n is less than the duration of seq n-1"

  [& seqs]
  (when-not (every? empty? seqs)
    (let [filled-seqs (filter (comp not empty?) seqs)
          seq-sort-criteria (fn [seq]
                              [(t/millis ((first seq) 0))
                               (- (- (t/millis ((first seq) 1)) (t/millis ((first seq) 0))))])
          seqs-order-by-head (sort-by seq-sort-criteria filled-seqs)
          first-period (ffirst seqs-order-by-head)
          rest-of-consumed (rest (first seqs-order-by-head))
          unconsumed (rest seqs-order-by-head)]
      (lazy-seq
       (cons first-period
             (apply combine (conj unconsumed rest-of-consumed)))))))
