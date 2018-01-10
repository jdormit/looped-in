(ns looped-in.build
  (:require [cljs.build.api :as cljs]))

(defn -main [& args]
  (cljs/build "src_cljs" {:optimizations :simple
                          :source-map true
                          :pretty-print true
                          :output-dir "ext/out"
                          :modules {:content {:output-to "ext/content.js"
                                              :entries #{"looped-in.content"}}
                                    :background {:output-to "ext/background.js"
                                                 :entries #{"looped-in.background"}}}}))
