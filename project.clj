(defproject monotony "0.0.3"
  :description "Time utilities for humans."
  :dependencies [[clojure "1.3.0"]
                 [org.clojure/core.logic "0.6.7"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [fidjet "0.0.1"]]
  :dev-dependencies [[com.stuartsierra/lazytest "2.0.0-SNAPSHOT"]
                     [lein-lazytest "1.0.3"]]
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"
                 "stuartsierra-snapshots" "http://stuartsierra.com/m2snapshots"}
  :lazytest-path ["src" "test"])
