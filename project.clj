(defproject conversations-extension "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [cljs-ajax "0.7.3"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :profiles {:cljs-shared
             {:cljsbuild
              {:builds
               {:main
                {:source-paths ["src"]
                 :compiler {:optimizations :simple
                            :pretty-print true
                            :source-map true
                            :output-dir "ext/js/generated/out"
                            :modules {:background
                                      {:output-to "ext/js/generated/background.js"
                                       :entries #{"looped-in.background"}}
                                      :content
                                      {:output-to "ext/js/generated/content.js"
                                       :entries #{"looped-in.content"}}
                                      :sidebar
                                      {:output-to "ext/js/generated/sidebar.js"
                                       :entries #{"looped-in.sidebar"}}}}}}}}
             :dev {:deoendencies [[com.cemerick/piggieback "0.2.2"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
