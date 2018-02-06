(defproject conversations-extension "1.0.1"
  :description "A browser extension that display Hackers News comments for the current web page"
  :url "https://github.com/jdormit/looped-in"
  :license {:name "GNU General Public License"
            :url "https://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.3.465"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [cljs-ajax "0.7.3"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :clean-targets ["ext/js/generated"]
  :aliases {"build" ["do" "clean" ["cljsbuild" "once"]]}
  :profiles {:cljs-shared
             {:cljsbuild
              {:builds
               {:main
                {:source-paths ["src"]
                 :compiler {:optimizations :simple
                            :pretty-print true
                            :source-map true
                            :output-dir "ext/js/generated/out"
                            :closure-output-charset "us-ascii"
                            :modules {:background
                                      {:output-to "ext/js/generated/background.js"
                                       :entries #{"looped-in.background"}}
                                      :content
                                      {:output-to "ext/js/generated/content.js"
                                       :entries #{"looped-in.content"}}
                                      :sidebar
                                      {:output-to "ext/js/generated/sidebar.js"
                                       :entries #{"looped-in.sidebar"}}}}}}}}
             :dev {:dependencies [[com.cemerick/piggieback "0.2.2"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
