(ns ^{:doc "Namespace for working with Monotony using with-config"
      :author "Alex Redington"}
  monotony.configured
  (:require [monotony.core :as m]
            [clojure.set :as s]))

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

(defmacro import-api-fns
  "Expose the core fns from api-ns in the namespace where this is called.
  All fns that need a parameter naemd thing-sym will be masked with
  obnoxious exception throwing behavior."
  [api-ns thing-sym]
  (let [fail-without-thing (fn [& args]
                             (throw (IllegalStateException. (str "Must be called from within a with-" (str thing-sym) " block."))))]
    `(do ~@(for [needs-thing (keys (thinged-fn-sym-vars (find-ns api-ns) thing-sym))]
             `(def ~(with-meta needs-thing {:dynamic true}) ~fail-without-thing))
         ~@(for [imports-fine (unthinged-fn-sym-vars (find-ns api-ns) thing-sym)]
             `(def ~(first imports-fine) ~(deref (second imports-fine)))))))
         ;; (defmacro
         ;;    ~(symbol (str "with-" thing-sym))
         ;;    ~(str "Evaluate body with all "
         ;;            *ns* " functions receiving "
         ;;            thing-sym " as their first argument")
         ;;    ~(vector thing-sym (symbol "&") (symbol "body"))
         ;;    `(with-bindings
         ;;       ~(into {} (for [~(symbol 'var-name) (keys (thinged-fn-sym-vars ~(find-ns api-ns) (quote ~thing-sym)))]
         ;;                   [`(var ~(symbol ~*ns* (str ~(symbol 'var-name))))
         ;;                    `(partial ~(symbol ~(find-ns api-ns) (str ~(symbol 'var-name))) (quote ~~thing-sym))]))
         ;;       ~@body)))))

(import-api-fns monotony.core config)

(defmacro with-config
  "Evaluate body with all monotony.configured functions
  receiving config as their first argument"
  [config & body]
  `(with-bindings
     ~(into {} (for [var-name (keys (thinged-fn-sym-vars 'monotony.core 'config))]
                 [`(var ~(symbol "monotony.configured" (str var-name)))
                  `(partial ~(symbol "monotony.core" (str var-name)) ~config)]))
     ~@body))
