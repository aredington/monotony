(ns ^{:doc "Monotony's temporal constants"
      :author "Alex Redington"}
  monotony.platform
  (:require [monotony.time :refer [millis period date]])
  #+clj (:import (java.util Calendar TimeZone Locale)))

(defprotocol Access
  (get-field [access config time] "Get the integer value for field `access` from `time` observing offsets from `config`.")
  (set-field [access config time value] "Create a new time with the value for field `access` on `time` set to `value`, observing offsets from `config`")
  (add-field [access config time amount] "Add `amount` to the present value of `time` for `access`, observing offsets from `config`")
  (get-name [access config time] "Get the name of the present value for `access`.")
  (transactional-set [access tx value]))

(def period-names
  "Inversion of named-periods to a map hierarchically nested for FIELD -> VALUE -> KEYWORD"
  #+clj {Calendar/DAY_OF_WEEK {Calendar/SUNDAY :sunday
                               Calendar/MONDAY :monday
                               Calendar/TUESDAY :tuesday
                               Calendar/WEDNESDAY :wednesday
                               Calendar/THURSDAY :thursday
                               Calendar/FRIDAY :friday
                               Calendar/SATURDAY :saturday}
         Calendar/MONTH {Calendar/JANUARY :january
                         Calendar/FEBRUARY :february
                         Calendar/MARCH :march
                         Calendar/APRIL :april
                         Calendar/MAY :may
                         Calendar/JUNE :june
                         Calendar/JULY :july
                         Calendar/AUGUST :august
                         Calendar/SEPTEMBER :september
                         Calendar/OCTOBER :october
                         Calendar/NOVEMBER :november
                         Calendar/DECEMBER :december}
         Calendar/HOUR_OF_DAY {0 :midnight
                               12 :noon}})

(defn all-period-names
  []
  (mapcat vals (vals period-names)))


#+clj (defn calendar
        "Returns a calendar instance initialized to time"
        [{:keys [calendar offset]} time]
        (let [^Calendar cal (cond
                             offset (Calendar/getInstance (TimeZone/getTimeZone offset) Locale/ROOT) 
                             calendar (calendar)
                             true (Calendar/getInstance))]
          (do
            (.setTimeInMillis cal (millis time))
            cal)))

#+clj (deftype PlatformAccess [field]
        Access
        (get-field [_ config time]
          (let [^Calendar cal (calendar config time)]
            (.get cal field)))
        (set-field [_ config time value]
          (let [^Calendar cal (calendar config time)]
            (.set cal field value)
            (.getTime cal)))
        (add-field [_ config time amount]
          (let [^Calendar cal (calendar config time)]
            (.add cal field amount)
            (.getTime cal)))
        (get-name [_ config time]
          (let [^Calendar cal (calendar config time)
                field-val (.get cal field)]
            (get-in period-names [field field-val])))
        (transactional-set [_ tx value]
          (.set tx field value)))

(defn create-date
  [config accessor-map]
  #+clj (let [^Calendar cal (calendar config ((:seed config)))]
          (.clear cal)
          (doseq [[accessor value] accessor-map]
            (transactional-set accessor cal value))
          (.getTime cal)))

(def cycles
  "Calendar constants which represent periods of time."
  #+clj {:millisecond (PlatformAccess. Calendar/MILLISECOND)
         :second (PlatformAccess. Calendar/SECOND)
         :minute (PlatformAccess. Calendar/MINUTE)
         :hour (PlatformAccess. Calendar/HOUR_OF_DAY)
         :day (PlatformAccess. Calendar/DAY_OF_WEEK)
         :week (PlatformAccess. Calendar/WEEK_OF_MONTH)
         :month (PlatformAccess. Calendar/MONTH)
         :year (PlatformAccess. Calendar/YEAR)})
