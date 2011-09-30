(ns ^{:doc "A composable take on generating times."
      :author "Alex Redington"} monotony.core
    (:import java.util.Date java.util.Calendar))

(defn now
  "Returns the current time"
  []
  (Date.))

(def *seed* ^{:doc "The function called to return the seed
  date. Defaults to fetching the system time."}
  now)

(defn new-cal
  "Return a new default calendar instance."
  []
  (Calendar/getInstance))

(defn blank-cal
  "Returns a calendar instance initialized to the UNIX epoch."
  []
  (let [blank-cal (Calendar/getInstance)]
    (.setTimeInMillis blank-cal 0)
    blank-cal))

(def *calendar* ^{:doc "The function called to return a new calendar
  instance. Defaults to using new-cal"}
  new-cal)

(defprotocol Time
  "An instant in time."
  (period [time duration] "Return two dates which mark the start an
  end points of a period of time.")
  (millis [time] "Return the number of milliseconds since Jan 1 1970, 12:00AM GMT.")
  (date [time] "Returns a date object equivalent to time."))

(extend-protocol Time
  Long
  (millis
    [time]
    time)
  (period
    [time duration]
    [(date time) (date (+ time duration))])
  (date
    [time]
    (Date. time))
  Date
  (millis
    [time]
    (.getTime time))
  (period
    [time duration]
    [time (date (+ (millis time) duration))])
  (date
    [time]
    time))

(def cycles
  ^{:doc "Calendar constants which represent periods of time."}
  {:millis Calendar/MILLISECOND
   :second Calendar/SECOND
   :minute Calendar/MINUTE
   :hour Calendar/HOUR_OF_DAY
   :day Calendar/DATE
   :week Calendar/WEEK_OF_YEAR
   :month Calendar/MONTH
   :year Calendar/YEAR})

(def named-periods
  ^{:doc "Periods with specific names, e.g. Monday, Tuesday, Friday, January, March"}
  {:sunday [Calendar/SUNDAY Calendar/DAY_OF_WEEK]
   :monday [Calendar/MONDAY Calendar/DAY_OF_WEEK]
   :tuesday [Calendar/TUESDAY Calendar/DAY_OF_WEEK]
   :wednesday [Calendar/WEDNESDAY Calendar/DAY_OF_WEEK]
   :thursday [Calendar/THURSDAY Calendar/DAY_OF_WEEK]
   :friday [Calendar/FRIDAY Calendar/DAY_OF_WEEK]
   :saturday [Calendar/SATURDAY Calendar/DAY_OF_WEEK]
   :january [Calendar/JANUARY Calendar/MONTH]
   :february [Calendar/FEBRUARY Calendar/MONTH]
   :march [Calendar/MARCH Calendar/MONTH]
   :april [Calendar/APRIL Calendar/MONTH]
   :may [Calendar/MAY Calendar/MONTH]
   :june [Calendar/JUNE Calendar/MONTH]
   :july [Calendar/JULY Calendar/MONTH]
   :august [Calendar/AUGUST Calendar/MONTH]
   :september [Calendar/SEPTEMBER Calendar/MONTH]
   :october [Calendar/OCTOBER Calendar/MONTH]
   :november [Calendar/NOVEMBER Calendar/MONTH]
   :december [Calendar/DECEMBER Calendar/MONTH]
   })

(def cycle-keywords
  ^{:doc "Special case Calendar mappings of inferior cycles."}
  {:week (list Calendar/MILLISECOND Calendar/SECOND Calendar/MINUTE Calendar/HOUR_OF_DAY Calendar/DAY_OF_WEEK)})

(defn contained-cycle-keywords
  "Return a list of all the cycle keywords contained by keyword.
  :year contains :month, :week, :day, :hour, :minute, and :second"
  [keyword]
  (if (contains? cycle-keywords keyword)
    (cycle-keywords keyword)
    (let [after-epoch (fn [kw]
                        (let [blank-cal (blank-cal)]
                          (.add blank-cal (kw cycles) 1)
                          (.getTimeInMillis blank-cal)))]
      (map #(get cycles %)
           (filter #(< (after-epoch %)
                       (after-epoch keyword)) (keys cycles))))))

(defn calendar
  "Returns a calendar instance initialized to time"
  [time]
  (let [cal (*calendar*)]
    (do
      (.setTimeInMillis cal (millis time))
      cal)))

(defn period-named?
  "Returns true if the period is the named period, false otherwise."
  [period name]
  (let [named-period (named-periods name)
        start-cal (calendar (period 0))
        end-cal (calendar (period 1))]
    (and (= (.get start-cal (named-period 1)) (named-period 0))
         (= (.get end-cal (named-period 1)) (named-period 0)))))

(defn later
  "Return a time cycle later than seed."
  ([amount cycle]
     (later amount cycle (*seed*)))
  ([amount cycle seed]
     (let [cal (calendar seed)]
       (do (.add cal (cycle cycles) amount)
           (.getTime cal)))))

(defn prior-boundary
  "Returns the start of a bounded cycle including time seed.

  (prior-boundary (now) :year)

  will return 12:00:00AM of the present year."
  [seed cycle]
  (let [cal (calendar seed)
        cycle-vals (reverse (contained-cycle-keywords cycle))]
    (doseq [contained-cycle-val cycle-vals]
      (.set cal contained-cycle-val
            (.getActualMinimum cal contained-cycle-val)))
    (.getTime cal)))

(defn next-boundary
  "Returns the next clean boundary of a cycle after
  time seed.

  (next-boundary (now) :year)

  will return 12:00:00AM on Jan 1st of the next year."
  [seed cycle]
  (prior-boundary (later 1 cycle seed) cycle))

(defn milli-before
  "Returns the date 1 millisecond before time."
  [time]
  (date (- (millis time) 1)))

(defn period-after
  "Returns a period of size equal to cycle, starting
  at seed."
  [seed cycle]
  (vector
   seed
   (milli-before (later 1 cycle seed))))

(defn cycles-in
  "Break a period down by a cycle into multiple sub-periods wholly
  contained in that period. The first sub-period will be inclusive of
  the start of the period. The last sub-period will be exclusive of
  the end of the period. Returns a lazy-seq of the periods."
  [period cycle]
  (let [start (period 0)
        end (period 1)]
    (lazy-seq
     (when (< (millis start) (millis end))
       (cons (period-after start cycle)
             (cycles-in [(later 1 cycle start) end] cycle))))))

(defn bounded-cycles-in
  "Break a period down by a cycle into multiple sub-periods, such
  that the boundaries between periods cleanly map onto calendar breaks,
  e.g. if month is bound to a period of one month

  (bounded-cycles-in month :week)

  Will return a seq of the first partial week of the month, all of the weeks
  starting with Sunday and ending with Saturday, and the last partial week of the month"
  [period cycle]
  (let [start (period 0)
        end (period 1)
        first-bounded (next-boundary start cycle)
        last-bounded (prior-boundary end cycle)
        start-fragment [start (milli-before first-bounded)]
        cycles-in-period (cycles-in
                          [first-bounded
                           (milli-before last-bounded)] cycle)
        end-fragment [last-bounded end]]
    (concat '(start-fragment)
            (cycles-in-period)
            '(end-fragment))))

(defn periods
  "Return an lazy infinite sequence of periods with a duration equal to cycle.
  If seed is provided, the first period will include it. Otherwise, period
  will include the result of calling *seed*"
  ([cycle]
     (periods cycle (*seed*)))
  ([cycle seed]
     (lazy-seq
      (cons (period-after (prior-boundary seed cycle) cycle)
            (periods cycle (later 1 cycle seed))))))
