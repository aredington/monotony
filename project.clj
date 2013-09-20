(defproject monotony "0.0.6"
  :description "Time utilities for humans."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/core.logic "0.6.7"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [fidjet "0.0.1"]]
  :license {:name "MIT"
            :url "https://raw.github.com/aredington/monotony/master/LICENSE"
            :distribution :repo}
  :url "http://github.com/aredington/monotony"
  :profiles {:dev  {:dependencies [[midje "1.5.1"]
                                   [clj-time "0.6.0"]]
                    :plugins [[lein-midje "3.1.0"]]}})
