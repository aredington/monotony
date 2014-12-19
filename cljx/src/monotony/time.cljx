(ns ^{:doc "Monotony's Time protocol"
      :author "Alex Redington"} monotony.time
      #+clj (:import (java.util Date Calendar)))

(defprotocol Time
  "An instant in time."
  (millis [time] "Return the number of milliseconds since Jan 1 1970, 12:00AM GMT.")
  (period [time duration]
    "Return two dates which mark the start an end points of a period
    of time.")
  (date [time]
    "Returns a date object equivalent to time."))

(defprotocol Period
  "All instants between an inclusive start and an inclusive end, with millisecond level resolution."
  (start [period] "Return the instant at the start of `period`")
  (end [period] "Return the instant at the end of `period`"))

(extend-protocol Period
  clojure.lang.APersistentVector
  (start [period] (first period))
  (end [period] (second period)))

#+clj (extend-protocol Time
        Long
        (millis [time] time)
        (period [time duration] [(date time) (date (+ time duration))])
        (date [time] (Date. time))
        Date
        (millis [time] (.getTime time))
        (period [time duration] [time (date (+ (millis time) duration))])
        (date [time] time))
#+cljs (extend-protocol Time
         number
         (millis [time] time)
         (period [time duration] [(date time) (date (+ time duration))])
         (date [time] (js/Date. time))
         js/Date
         (millis [time] (.getTime time))
         (period [time duration] [time (date (+ (millis time) duration))])
         (date [time] time))

#+clj (defmacro extend-if
        "If `class` resolves to a class, extend `protocol` to it with `specs`."
        [class protocol & specs]
        (when-let [joda-time-class (try (Class/forName (name class))
                                        (catch java.lang.ClassNotFoundException e
                                          nil))]
          `(extend-type ~joda-time-class ~protocol ~@specs)))

#+clj (extend-if org.joda.time.base.AbstractInstant
                 Time
                 (millis [time]
                         (.getMillis time))
                 (period [time duration]
                         [(date time) (date (+ (millis time) duration))])
                 (date [time]
                       (.toDate time)))

#+clj (extend-if org.joda.time.base.AbstractInterval
                 Period
                 (start [interval]
                        (date (.getStart interval)))
                 (end [interval]
                      (date (.getEnd interval))))
