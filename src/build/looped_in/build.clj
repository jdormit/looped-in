(ns looped-in.build
  (:require [cljs.build.api :as cljs]))

(defn build
  [root opts]
  (println (str "Compiling " root " to " (:output-to opts)))
  (cljs/build root opts)
  (println (str "Compiled " (:output-to opts))))

(defn -main [& args]
  (build "src/content" {:output-to "ext/content.js"
                        :optimizations :simple
                        :closure-output-charset "US-ASCII"
                        :pretty-print true})
  (build "src/background" {:output-to "ext/background.js"
                           :optimizations :simple
                           :closure-output-charset "US-ASCII"
                           :pretty-print true}))
