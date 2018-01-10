(ns looped-in.build
  (:require [cljs.build.api :refer [build]]))

(defn -main [& args]
  (build "src/content" {:output-to "ext/content.js"
                        :optimizations :simple
                        :closure-output-charset "US-ASCII"
                        :pretty-print true})
  (build "src/background" {:output-to "ext/background.js"
                           :optimizations :simple
                           :closure-output-charset "US-ASCII"
                           :pretty-print true}))
