(ns looped-in.background
  (:require [clojure.core.match :refer [match]]
            [cljs.core.async :refer [go chan >! <!]]
            [ajax.core :refer [GET]]
            [looped-in.logging :as log]
            [looped-in.promises :refer [channel->promise]]))

(enable-console-print!)

(def browser-action (.-browserAction js/browser))

(defn fetch-submission
  "Fetches submissions from Hacker News by `url`"
  [url]
  (let [response-chan (chan)]
    (GET "https://hn.algolia.com/api/v1/search"
         {:params {"query" url
                   "hitsPerPage" 1000
                   "restrictSearchableAttributes" "url"}
          :handler (fn [res] (go (>! response-chan res)))
          :error-handler (fn [err] (log/error "Error fetching HN stories:"
                                              (clj->js err)))})
    response-chan))

(defn handle-message
  "Handles messages from the content script"
  [msg]
  (match (.-type msg)
         "fetchData" (channel->promise (fetch-submission (.-url msg)))
         x (log/warn "Ignoring unknown message type" x)))

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener handle-message
     #_(fn [msg]
         (prn msg)
         (.setBadgeBackgroundColor browser-action #js {:color "#232323"})
         (.setBadgeText browser-action
                        #js {:text (str (.-numComments msg))}))))
