(ns ^{:doc "Namespace for working with Monotony using with-config"
      :author "Alex Redington"}
  monotony.configured
  (:require [monotony.core :as m]
            [monotony.util :as u]))

(defmacro import-core-fns
  "Expose the core fns from monotony.core in the monotony.configured namespace.
  All fns that need configuration will be masked with obnoxious exception throwing
  behavior."
  []
  (let [fail-without-config (fn [& args]
                              (throw (IllegalStateException. "Must be called from within a with-config block.")))]
    `(do ~@(for [needs-config (keys (u/configured-fn-sym-vars))]
             `(def ~(with-meta needs-config {:dynamic true}) ~fail-without-config))
         ~@(for [import-fine (u/unconfigured-fn-sym-vars)]
             `(def ~(first import-fine) ~(deref (second import-fine)))))))

(import-core-fns)

(defmacro with-config
  "Given config, evaluate body with all monotony.configured functions
  receiving config as their first argument"
  [config & body]
  `(with-bindings
     ~(into {} (for [var-name (keys (u/configured-fn-sym-vars))]
                 [`(var ~(symbol "monotony.configured" (str var-name)))
                  `(partial ~(symbol "monotony.core" (str var-name)) ~config)]))
     ~@body))
