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
  "Return the names and vars of all fns that accept thing-sym as their
  first arg in api-ns"
  [api-ns thing-sym]
  (filter
   ;; Thise absurdly convoluted predicate will dig through api-ns, and
   ;; find every fn where every arity receives thing-sym as its first
   ;; arg.
   (comp (partial every? #(= thing-sym (first %))) :arglists meta second)
   (api-fn-sym-vars api-ns)))

(defn unthinged-fn-sym-vars
  "Return the names and vars of all the fns that don't accept
  thing-sym as their first arg in api-ns"
  [api-ns thing-sym]
  (s/difference (set (api-fn-sym-vars api-ns)) (set (thinged-fn-sym-vars api-ns thing-sym))))

(defn thingy-bindings
  "Create a bindings map for all fns that accept thing-sym as their
  first arg from source-ns, binding partial versions of them them in
  target-ns with thing as their first arg"
  [source-ns target-ns thing-sym thing]
  (into {} (for [var-name (keys (thinged-fn-sym-vars source-ns thing-sym))]
             [(ns-resolve target-ns var-name) `(partial
                                               ~(ns-resolve source-ns var-name)
                                               ~thing)])))

(defmacro make-the-with-thingy
  "Create a with-thing-sym macro for executing all of the matching
  functions from api-ns with the first argument passed from the
  with-thing-sym block. e.g.:

  (make-the-with-thingy monotony.core config)

  will make a with-config macro against the monotony.core namespace."
  [api-ns thing-sym]
  `(defmacro  ~(symbol (str "with-" thing-sym))
     ~(str "Evaluate body with all "
           *ns* " functions receiving "
           thing-sym " as their first argument")
     ~(vector thing-sym (symbol "&") (symbol "body"))
     `(with-bindings ~(thingy-bindings (quote ~(ns-name api-ns)) (quote ~(ns-name *ns*)) (quote ~thing-sym) ~thing-sym)
        ~@~(symbol "body"))))

(defmacro import-api-fns
  "Expose the core fns from api-ns in the namespace where this is called.
  All fns that need a parameter naemd thing-sym will be masked with
  obnoxious exception throwing behavior."
  [api-ns thing-sym]
  (let [fail-without-thing (fn [fn-name]
                             (fn [& args]
                               (throw (IllegalStateException.
                                       (str (str fn-name) " must be called from within a with-"
                                            (str thing-sym) " block.")))))]
    ;; Import the fns from api-ns
    `(do ~@(for [needs-thing (keys (thinged-fn-sym-vars (find-ns api-ns) thing-sym))]
             `(def ~(with-meta needs-thing {:dynamic true}) ~(fail-without-thing needs-thing)))
         ~@(for [imports-fine (unthinged-fn-sym-vars (find-ns api-ns) thing-sym)]
             `(def ~(first imports-fine) ~(deref (second imports-fine)))))))

(defmacro do-all-the-things
  [api-ns thing-sym]
  `(do
     (import-api-fns ~api-ns ~thing-sym)
     (make-the-with-thingy ~api-ns ~thing-sym)))

(do-all-the-things monotony.core config)

