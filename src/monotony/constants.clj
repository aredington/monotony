(ns ^{:doc "Monotony's temporal constants"
      :author "Alex Redington"}
  monotony.constants
  (:import java.util.Calendar))

(def cycles
  ^{:doc "Calendar constants which represent periods of time."}
  {:millisecond Calendar/MILLISECOND
   :second Calendar/SECOND
   :minute Calendar/MINUTE
   :hour Calendar/HOUR_OF_DAY
   :day Calendar/DAY_OF_WEEK
   :week Calendar/WEEK_OF_MONTH
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
