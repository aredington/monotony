(defproject monotony "0.0.7"
  :description "Time utilities for humans."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.logic "0.8.8"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/clojurescript "0.0-2496"]]
  :profiles {:dev {:dependencies [[clj-time "0.6.0"]
                                  [com.cemerick/clojurescript.test "0.3.3"]]
                   :cljsbuild {:builds [#_{:source-paths ["target/src" "target/test"]
                                           :compiler {:output-to "target/js/monotony_test.js"
                                                      :optimizations :whitespace
                                                      :pretty-print true}}]
                               #_:test-commands #_{"unit-tests" ["phantomjs" :runner "target/js/monotony_test.js"]}}}}
  :license {:name "MIT"
            :url "https://raw.github.com/aredington/monotony/master/LICENSE"
            :distribution :repo}
  :plugins [[com.keminglabs/cljx "0.5.0"]
            [lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.3"]]
  :source-paths ["src" "target/src"]
  :test-paths ["test" "target/test"]
  :cljx {:builds [{:source-paths ["cljx/src"]
                   :output-path "target/src"
                   :rules :clj}
                  {:source-paths ["cljx/test"]
                   :output-path "target/test"
                   :rules :clj}
                  {:source-paths ["cljx/src"]
                   :output-path "target/src"
                   :rules :cljs}
                  {:source-paths ["cljx/test"]
                   :output-path "target/test"
                   :rules :cljs}]}
  :cljsbuild {:builds [{:source-paths ["target/src"]
                        :compiler {:output-to "target/js/monotony.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :prep-tasks [["cljx" "once"] "javac" "compile"]
  :hooks [leiningen.cljsbuild]
  :url "http://github.com/aredington/monotony")
