(ns ^{:doc "Namespace for working with Monotony using with-config"
      :author "Alex Redington"}
  monotony.configured
  (:require [monotony.core :as m]
            [clojure.set :as s :only (difference)]))

;(def conf (monotony.core/new-config))

(defn- core-fn-sym-vars
  "Retrieve the names and vars of all fns defined in core"
  []
  (filter (comp fn? deref second) (ns-publics 'monotony.core)))

(defn- configured-fn-sym-vars
  "Retrieve the names and vars of all fns that need configuring in core"
  []
  (filter (comp :configured meta second) (core-fn-sym-vars)))

(defn- unconfigured-fn-sym-vars
  "Retrieve the names and vars of all the fns that don't need a config
  object in core"
  []
  (s/difference (set (core-fn-sym-vars)) (set (configured-fn-sym-vars))))

(defmacro import-core-fns []
  (let [fail-without-config (fn [& args]
                              (throw (IllegalStateException. "Must be called from within a with-config block.")))]
    `(do ~@(for [needs-config (keys (configured-fn-sym-vars))]
             `(def ~(with-meta needs-config {:dynamic true}) ~fail-without-config))
         ~@(for [import-fine (unconfigured-fn-sym-vars)]
             `(def ~(first import-fine) ~(deref (second import-fine)))))))

(import-core-fns)

(defmacro with-config
  [config & body]
  `(with-bindings
     ~(into {} (for [var-name (keys (configured-fn-sym-vars))]
                 [`(var ~(symbol "monotony.configured" (str var-name)))
                  `(partial ~(symbol "monotony.core" (str var-name)) ~config)]))
     ~@body))
