(defproject monotony "0.0.6-SNAPSHOT"
  :description "Time utilities for humans."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/core.logic "0.6.7"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [fidjet "0.0.1"]]
  :profiles {:dev  {:dependencies [[midje "1.5.1"]]
                    :plugins [[lein-midje "3.1.0"]]}})
