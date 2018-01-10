(defproject conversations-extension "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.465"]
                 [cljs-ajax "0.7.3"]]
  :source-paths ["src_clj"]
  :main looped-in.build
  :aliases {"build" ["run"]})
