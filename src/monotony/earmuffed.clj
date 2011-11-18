(ns ^{:doc "Namespace for working with Monotony using earmuffs"
      :author "Alex Redington"}
  monotony.earmuffed
  (:require [monotony.core :as m]
            [monotony.util :as u])
  (:import java.util.Calendar java.util.Date))

(defn now
  "Returns the current time"
  []
  (Date.))

(defn local-cal
  "Returns a new Calendar in the system locale and timezone"
  []
  (Calendar/getInstance))

(def ^:dynamic *seed* ^{:doc "The function called to return the seed
  date. Defaults to fetching the system time."}
  now)

(def ^:dynamic *calendar* ^{:doc "The function called to return a new calendar
  instance. Defaults to using new-cal"}
  local-cal)

(defmacro earmuff-core-fns
  "Expose the core fns from monotony.core in the monotony.earmuffed namespace.
  All fns that need configuration will read dynamic special vars to retrieve their values."
  []
  (let [earmuff-config (reify clojure.lang.ILookup
                         (valAt [this key] (case key
                                                 :seed *seed*
                                                 :calendar *calendar*
                                                 nil)))]
    `(do ~@(for [needs-config (keys (u/thinged-fn-sym-vars 'monotony.core 'config))]
             `(def ~needs-config (partial ~(symbol "monotony.core" (str needs-config)) ~earmuff-config)))
         ~@(for [import-fine (u/unthinged-fn-sym-vars 'monotony.core 'config)]
             `(def ~(first import-fine) ~(deref (second import-fine)))))))

(earmuff-core-fns)
