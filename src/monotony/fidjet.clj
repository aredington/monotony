(ns ^{:doc "Fidjet: Making pure fns with configurations less painful."
      :author "Alex Redington"}
  monotony.fidjet
  (:require [clojure.set :as s]))

(defn fn-sym-vars
  "Return the names and vars of all fns defined in ns"
  [ns]
  (filter (comp fn? deref second) (ns-publics ns)))

(defn fn-sym-vars-with-arg
  "Return the names and vars of all fns that accept arg-sym as their
  first arg in ns"
  [ns arg-sym]
  (filter
   ;; Thise absurdly convoluted predicate will dig through ns, and
   ;; find every fn where every arity receives arg-sym as its first
   ;; arg.
   (comp (partial every? #(= arg-sym (first %))) :arglists meta second)
   (fn-sym-vars ns)))

(defn fn-sym-vars-without-arg
  "Return the names and vars of all the fns that don't accept
  arg-sym as their first arg in ns"
  [ns arg-sym]
  (s/difference (set (fn-sym-vars ns)) (set (fn-sym-vars-with-arg ns arg-sym))))

(defn override-bindings
  "Create a bindings map for all fns that accept arg-sym as their
  first arg from source-ns, binding partial versions of them them in
  target-ns with arg as their first arg"
  [source-ns target-ns arg-sym arg]
  (into {} (for [var-name (keys (fn-sym-vars-with-arg source-ns arg-sym))]
             [(ns-resolve target-ns var-name) `(partial
                                               ~(ns-resolve source-ns var-name)
                                               ~arg)])))

(defmacro make-with-arg-macro
  "Create a with-arg macro for executing all of the matching functions
  from ns with the first argument passed implicitly e.g.:

  (make-with-arg-macro monotony.core config)

  will make a with-config macro against the monotony.core namespace."
  [ns arg-sym]
  `(defmacro  ~(symbol (str "with-" arg-sym))
     ~(str "Evaluate body with all "
           *ns* " functions receiving "
           arg-sym " as their first argument")
     ~(vector arg-sym (symbol "&") (symbol "body"))
     `(with-bindings ~(override-bindings (quote ~(ns-name ns)) (quote ~(ns-name *ns*)) (quote ~arg-sym) ~arg-sym)
        ~@~(symbol "body"))))

(defmacro import-api-fns
  "Expose the fns from ns in the namespace where this is called.
  All fns that need a first arg named arg-sym will be masked with
  obnoxious exception throwing behavior."
  [ns arg-sym]
  (letfn [(fail-without-arg [fn-name]
            (fn [& args]
              (throw (IllegalStateException.
                      (str (str fn-name) " must be called from within a with-"
                           (str arg-sym) " block.")))))]
    ;; Import the fns from api-ns
    `(do ~@(for [needs-arg (keys (fn-sym-vars-with-arg (find-ns ns) arg-sym))]
             `(def ~(with-meta needs-arg {:dynamic true}) ~(fail-without-arg needs-arg)))
         ~@(for [imports-fine (fn-sym-vars-without-arg (find-ns ns) arg-sym)]
             `(def ~(first imports-fine) ~(deref (second imports-fine)))))))

(defmacro remap-ns-with-arg
  [ns arg-sym]
  `(do
     (import-api-fns ~ns ~arg-sym)
     (make-with-arg-macro ~ns ~arg-sym)))
