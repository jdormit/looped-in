(defproject conversations-extension "1.1.1"
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
  :profiles {:cljs-shared
             {:cljsbuild
              {:builds
               {:background
                {:source-paths ["src/background" "src/lib"]
                 :compiler {:output-dir "ext/js/generated/out-background"
                            :closure-output-charset "us-ascii"
                            :main looped-in.background
                            :output-to "ext/js/generated/background.js"}}
                :content
                {:source-paths ["src/content" "src/lib"]
                 :compiler {:output-dir "ext/js/generated/out-content"
                            :closure-output-charset "us-ascii"
                            :main looped-in.content
                            :output-to "ext/js/generated/content.js"}}
                :sidebar
                {:source-paths ["src/sidebar" "src/lib"]
                 :compiler {:output-dir "ext/js/generated/out-sidebar"
                            :closure-output-charset "us-ascii"
                            :main looped-in.sidebar
                            :output-to "ext/js/generated/sidebar.js"}}}}}
             :dev [:cljs-shared
                   {:cljsbuild
                    {:builds
                     {:background
                      {:compiler {:optimizations :whitespace
                                  :pretty-print true
                                  :source-map "ext/js/generated/background.js.map"}}
                      :content
                      {:compiler {:optimizations :whitespace
                                  :pretty-print true
                                  :source-map "ext/js/generated/content.js.map"}}
                      :sidebar
                      {:compiler {:optimizations :whitespace
                                  :pretty-print true
                                  :source-map "ext/js/generated/sidebar.js.map"}}}}}
                   {:dependencies [[com.cemerick/piggieback "0.2.2"]
                                   [org.clojure/tools.nrepl "0.2.10"]]
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}]
             :prod [:cljs-shared
                    {:cljsbuild
                     {:builds
                      {:background
                       {:compiler {:optimizations :simple
                                   :pretty-print false
                                   :source-map false}}
                       :content
                       {:compiler {:optimizations :simple
                                   :pretty-print false
                                   :source-map false}}
                       :sidebar
                       {:compiler {:optimizations :simple
                                   :pretty-print false
                                   :source-map false}}}}}]})
