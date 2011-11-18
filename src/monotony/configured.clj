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

(import-api-fns monotony.core config)

(defn- thingy-bindings
  "Create a bindings map for all fns from source-ns in target-ns that accept thing-sym as
  their first arg"
  [source-ns target-ns thing-sym]
  (into {} (for [var-name (keys (thinged-fn-sym-vars source-ns thing-sym))]
             [(ns-resolve target-ns var-name) `(list 'partial
                                                    ~(ns-resolve source-ns var-name)
                                                    ~thing-sym)])))

(defmacro make-the-with-thingy
  "Create a with-thing-sym macro for executing all of the matching
  functions from api-ns with the first argument passed from the with-thing-sym block. e.g.:

  (make-the-with-thingy monotony.core config)

  will make a with-config macro against the monotony.core namespace."
  [api-ns thing-sym]
  (let [thingy-bindings (thingy-bindings (find-ns api-ns) *ns* thing-sym)]
    `(defmacro  ~(symbol (str "with-" thing-sym))
       ~(str "Evaluate body with all "
             *ns* " functions receiving "
             thing-sym " as their first argument")
       ~(vector thing-sym (symbol "&") (symbol "body"))
       `(with-bindings ~~thingy-bindings
          ~@~(symbol "body")))))

(make-the-with-thingy monotony.core config)
