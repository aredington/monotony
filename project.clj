(defproject monotony "0.0.7"
  :description "Time utilities for humans."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.logic "0.8.8"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [fidjet "0.0.1"]]
  :license {:name "MIT"
            :url "https://raw.github.com/aredington/monotony/master/LICENSE"
            :distribution :repo}
  :url "http://github.com/aredington/monotony"
  :profiles {:dev {:dependencies [[clj-time "0.6.0"]]}})
