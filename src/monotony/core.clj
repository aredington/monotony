(ns ^{:doc "A composable take on generating times in the future." :author "Alex Redington"} monotony.core
    (:import java.util.Date java.util.Calendar))

(defn now
  "Returns the current time"
  []
  (Date.))

(def *seed*
  ^{:doc "The function called to return the seed date. Defaults to fetching the system time."}
  now)

(defn new-cal
  "Return a new default calendar instance."
  []
  (Calendar/getInstance))

(defn blank-cal
  "Returns a calendar instance initialized to the UNIX epoch."
  []
  (let [blank-cal (Calendar/getInstance) ]
    (.setTimeInMillis blank-cal 0)
    blank-cal))

(def *calendar*
  ^{:doc "The function called to return a new calendar instance. Defaults to using new-cal"}
  new-cal)

(defprotocol Time
  "An instant in time."
  (period [time duration] "Return two dates which mark the start an end points of a period of time.")
  (millis [time] "Return the number of milliseconds since 1970."))

(extend-protocol Time
  Long
  (millis
    [time]
    time)
  (period
    [time duration]
    [(Date. time) (Date. (+ time duration))])
  Date
  (millis
    [time]
    (.getTime time))
  (period
    [time duration]
    [time (Date. (+ (millis time) duration))]))

(def cycles
  ^{:doc "Calendar constants which represent periods of time."}
  {:second Calendar/SECOND
   :minute Calendar/MINUTE
   :hour Calendar/HOUR
   :day Calendar/DATE
   :week Calendar/WEEK_OF_YEAR
   :month Calendar/MONTH
   :year Calendar/YEAR})

(defn contained-cycle-keywords
  "Return a list of all the cycle keywords contained by keyword.
  :year contains :month, :week, :day, :hour, :minute, and :second"
  [keyword]
  (let [after-epoch (fn [kw]
                      (let [blank-cal (blank-cal)]
                        (.add blank-cal (kw cycles) 1)
                        (.getTimeInMillis blank-cal)))]
      (filter #(< (after-epoch %) (after-epoch keyword)) (keys cycles))))

(defn calendar
  "Returns a calendar instance initialized to time"
  [time]
  (let [cal (*calendar*)]
    (do
      (.setTimeInMillis cal (millis time))
      cal)))

(defn next-boundary
  "Returns the next clean boundary of a cycle after
  time seed.

  (next-boundary (now) :year)

  will return 12:00:00AM on Jan 1st of the next year."
  [seed cycle]
  (let [cal (calendar seed)
        cycle-vals (reverse (map #(% cycles) (contained-cycle-keywords cycle)))
        cycle-val (cycle cycles)]
    (doseq [contained-cycle-val cycle-vals]
      (.set cal contained-cycle-val
            (.getActualMinimum cal contained-cycle-val)))
    (.add cal (cycle cycles) 1)
    (.getTime cal)))

(defn later
  "Return a time "
  ([amount cycle]
     (later amount cycle (*seed*)))
  ([amount cycle seed]
     (let [cal (calendar seed)]
       (do (.add cal (cycle cycles) amount)
           (.getTime cal)))))

(defn cycles-in
  "Break a period down by a cycle into multiple sub-periods wholly contained in that period.
  The first sub-period will be inclusive of the start of the period if appropriate. The last sub-period will be
  exclusive of the end of the period if appropriate. Returns a lazy-seq of the periods."
  [period cycle]
  ())
