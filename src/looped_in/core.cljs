(ns looped-in.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ajax.core :refer [GET]]
            [cljs.core.async :as async :refer [chan <! >!]]))

(enable-console-print!)

(defn fetch-submission
  "Fetches submissions from Hacker News by `url`"
  [url]
  (let [response-chan (chan)]
    (GET "http://hn.algolia.com/api/v1/search"
         {:params {"query" url
                   "hitsPerPage" 1000
                   "restrictSearchableAttributes" "url"}
          :handler (fn [res] (go (>! response-chan res)))
          :error-handler (fn [err] (go (>! response-chan err)))})
    response-chan))

(let [current-url (-> js/window (.-location) (.-href))
      sub-chan (fetch-submission current-url)]
  (go (console.log (clj->js (<! sub-chan)))))
