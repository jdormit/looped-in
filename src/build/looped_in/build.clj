(ns looped-in.build
  (:require [cljs.build.api :refer [build]]))

(defn -main [& args]
  (build "src/content" {:output-to "ext/content.js"
                        :output-dir "out"
                        :closure-output-charset "US-ASCII"
                        :optimizations :whitespace
                        :pretty-print true})
  (build "src/background" {:output-to "ext/background.js"
                           :output-dir "out"
                           :closure-output-charset "US-ASCII"
                           :optimizations :whitespace
                           :pretty-print true}))
