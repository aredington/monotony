(ns ^{:doc "Functions that return sequences of holidays."
      :author "Alex Redington"}
  monotony.holidays
  (:require [monotony.time :as t]
            [monotony.constants :as c]
            [monotony.core :as m]))

(defn- yearly-days-for-month
  "Returns a lazy seq of seqs, with one of the contained seqs
  containing all the days in the month named month-name."
  [config  month-name]
  (letfn [(months [config] (map #(m/bounded-cycles-in config % :month) (m/periods config :year)))
          (months-named [config month-name] (map
                                             (comp first (partial filter #(m/period-named? config % month-name)))
                                             (months config)))
          (days-for-month [config month] (m/bounded-cycles-in config month :day))]
    (map (partial days-for-month config) (months-named config month-name))))

(defn new-years-days
  "Returns a lazy seq of New Years day on each year."
  [config]
  (map first (yearly-days-for-month config :january)))

(defn martin-luther-king-birthdays
  "Returns a lazy seq of the observances of Martin Luther King, Jr.'s birthday"
  [config]
  (letfn
      [(mlk-observance [config days] (nth (filter #(m/period-named? config % :monday) days) 2))]
    (map (partial mlk-observance config) (yearly-days-for-month config :january))))

(defn washington-birthdays
  "Returns a lazy seq of the observances of George Washington's birthday"
  [config]
  (letfn
      [(washington-observance [config days] (nth (filter #(m/period-named? config % :monday) days) 2))]
    (map (partial washington-observance config) (yearly-days-for-month config :february))))

(defn memorial-days
  "Returns a lazy seq of Memorial Days"
  [config]
  (letfn
      [(memorial-day [config days] (last (filter #(m/period-named? config % :monday) days)))]
    (map (partial memorial-day config) (yearly-days-for-month config :may))))

(defn independence-days
  "Returns a lazy seq of the 4ths of July"
  [config]
  (letfn
      [(independence-day [config days] (nth days 3))]
    (map (partial independence-day config) (yearly-days-for-month config :july))))

(defn labor-days
  "Returns a lazy seq of Labor Days"
  [config]
  (letfn
      [(labor-day [config days] (first (filter #(m/period-named? config % :monday) days)))]
    (map (partial labor-day config) (yearly-days-for-month config :september))))

(defn columbus-days
  "Returns a lazy seq of Columbus Days"
  [config]
  (letfn
      [(columbus-day [config days] (nth (filter #(m/period-named? config % :monday) days) 1))]
    (map (partial columbus-day config) (yearly-days-for-month config :october))))

(defn veterans-days
  "Returns a lazy seq of Veterans Days"
  [config]
  (letfn
      [(veterans-day [config days] (nth days 10))]
    (map (partial veterans-day config) (yearly-days-for-month config :november))))

(defn thanksgivings
  "Returns a lazy seq of Thanksgivings"
  [config]
  (letfn
      [(thanksgiving [config days] (nth (filter #(m/period-named? config % :thursday) days) 3))]
    (map (partial thanksgiving config) (yearly-days-for-month config :november))))

(defn christmasses
  "Returns a lazy seq of Christmasses"
  [config]
  (letfn
      [(christmas [config days] (nth days 24))]
    (map (partial christmas config) (yearly-days-for-month config :december))))

(defn us-holidays
  "Returns a lazy seq of Federal Holidays in the United States"
  [config]
  (m/combine
   (new-years-days config)
   (martin-luther-king-birthdays config)
   (washington-birthdays config)
   (memorial-days config)
   (independence-days config)
   (labor-days config)
   (columbus-days config)
   (veterans-days config)
   (thanksgivings config)
   (christmasses config)))
