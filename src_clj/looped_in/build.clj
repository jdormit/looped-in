(ns looped-in.build
  (:require [cljs.build.api :as cljs]))

(defn -main [& args]
  (cljs/build "src_cljs" {:optimizations :simple
                          :source-map true
                          :pretty-print true
                          :output-dir "ext/js/generated/out"
                          :modules {:content {:output-to "ext/js/generated/content.js"
                                              :entries #{"looped-in.content"}}
                                    :background {:output-to "ext/js/generated/background.js"
                                                 :entries #{"looped-in.background"}}}}))
