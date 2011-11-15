(ns ^{:doc "Namespace for general monotonous utility functions."
      :author "Alex Redington"}
  monotony.util
  (:require [clojure.set :as s :only (difference)]))

(defn core-fn-sym-vars
  "Return the names and vars of all fns defined in core"
  []
  (filter (comp fn? deref second) (ns-publics 'monotony.core)))

(defn configured-fn-sym-vars
  "Return the names and vars of all fns that need configuring in core"
  []
  (filter (comp :configured meta second) (core-fn-sym-vars)))

(defn unconfigured-fn-sym-vars
  "Return the names and vars of all the fns that don't need a config
  object in core"
  []
  (s/difference (set (core-fn-sym-vars)) (set (configured-fn-sym-vars))))

