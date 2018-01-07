(defproject conversations-extension "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [cljs-ajax "0.7.3"]
                 [org.clojure/core.async "0.3.465"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :cljsbuild {:builds
              [{:source-paths ["src"]
                :compiler {:output-to "ext/main.js"
                           :output-dir "out"
                           :optimizations :whitespace}}]})
