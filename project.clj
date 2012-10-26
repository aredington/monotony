(defproject monotony "0.0.5"
  :description "Time utilities for humans."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/core.logic "0.6.7"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [fidjet "0.0.1"]]
  :profiles {:dev  {:dependencies [[com.stuartsierra/lazytest "2.0.0-SNAPSHOT"]]
                    :plugins [[lein-clojars "0.9.1"]]}}
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"
                 "stuartsierra-snapshots" "http://stuartsierra.com/m2snapshots"})
