(ns looped-in.hackernews
  (:require [cljs.core.async :refer [go go-loop >! chan close!]]
            [ajax.core :refer [GET]]
            [looped-in.logging :as log]))

(defn fetch-submission
  "Fetches submissions from Hacker News by `url`"
  [url]
  (let [response-chan (chan)]
    (GET "https://hn.algolia.com/api/v1/search"
         {:params {"query" url
                   "hitsPerPage" 1000
                   "restrictSearchableAttributes" "url"}
          :handler (fn [res] (go (>! response-chan res)))
          :error-handler (fn [err]
                           (log/error (str "Error fetching HN stories for " url ":") err)
                           (close! response-chan))})
    response-chan))

(defn fetch-item
  "Fetches items from Hacker News by `id`"
  [id]
  (let [response-chan (chan)]
    (GET (str "https://hn.algolia.com/api/v1/items/" id)
         {:handler (fn [res] (go (>! response-chan res)))
          :error-handler (fn [err]
                           (log/error (str "Error fetching item " id ":") err)
                           (close! response-chan))})
    response-chan))

(defn fetch-items [ids]
  (let [chans (map (fn [id]
                     (fetch-item id))
                   ids)]
    (go-loop [[channel & rest] chans
              acc []]
      (if (nil? channel)
        acc
        (recur rest (conj acc (<! channel)))))))
