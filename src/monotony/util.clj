(ns ^{:doc "Namespace for general monotonous utility functions."
      :author "Alex Redington"}
  monotony.util
  (:require [clojure.set :as s :only (difference)]))

(defn api-fn-sym-vars
  "Return the names and vars of all fns defined in api-ns"
  [api-ns]
  (filter (comp fn? deref second) (ns-publics api-ns)))

(defn thinged-fn-sym-vars
  "Return the names and vars of all fns that accept thing-sym as their first arg in api-ns"
  [api-ns thing-sym]
  (filter
   ;; Thise absurdly convoluted predicate will dig through api-ns, and
   ;; find every fn where every arity receives thing-sym as its first
   ;; arg.
   (comp (partial every? #(= thing-sym (first %))) :arglists meta second)
   (api-fn-sym-vars api-ns)))

(defn unthinged-fn-sym-vars
  "Return the names and vars of all the fns that don't need a config
  object in core"
  [api-ns thing-sym]
  (s/difference (set (api-fn-sym-vars api-ns)) (set (thinged-fn-sym-vars api-ns thing-sym))))
