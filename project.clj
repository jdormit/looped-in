(defproject conversations-extension "1.1.2"
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
                 :compiler {:closure-output-charset "us-ascii"
                            :main looped-in.background}}
                :content
                {:source-paths ["src/content" "src/lib"]
                 :compiler {:closure-output-charset "us-ascii"
                            :main looped-in.content}}
                :sidebar
                {:source-paths ["src/sidebar" "src/lib"]
                 :compiler {:closure-output-charset "us-ascii"
                            :main looped-in.sidebar}}}}}
             :dev [:cljs-shared
                   {:cljsbuild
                    {:builds
                     {:background
                      {:compiler {:optimizations :none
                                  :output-to "resources/dev/js/generated/background.js"
                                  :output-dir "resources/dev/js/generated/out-background"
                                  :asset-path "js/generated/out-background"
                                  :pretty-print true
                                  :source-map true}}
                      :content
                      {:compiler {:optimizations :whitespace
                                  :output-to "resources/dev/js/generated/content.js"
                                  :output-dir "resources/dev/js/generated/out-content"
                                  :pretty-print true
                                  :source-map "resources/dev/js/generated/content.js.map"}}
                      :sidebar
                      {:compiler {:optimizations :none
                                  :output-to "resources/dev/js/generated/sidebar.js"
                                  :output-dir "resources/dev/js/generated/out-sidebar"
                                  :asset-path "js/generated/out-sidebar"
                                  :pretty-print true
                                  :source-map true}}}}}]
             :prod [:cljs-shared
                    {:cljsbuild
                     {:builds
                      {:background
                       {:compiler {:optimizations :simple
                                   :output-to "resources/prod/js/generated/background.js"
                                   :output-dir "resources/prod/js/generated/out-background"
                                   :pretty-print false
                                   :source-map false}}
                       :content
                       {:compiler {:optimizations :simple
                                   :output-to "resources/prod/js/generated/content.js"
                                   :output-dir "resources/prod/js/generated/out-content"
                                   :pretty-print false
                                   :source-map false}}
                       :sidebar
                       {:compiler {:optimizations :simple
                                   :output-to "resources/prod/js/generated/sidebar.js"
                                   :output-dir "resources/prod/js/generated/out-sidebar"
                                   :pretty-print false
                                   :source-map false}}}}}]})
