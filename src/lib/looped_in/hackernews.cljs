;; Copyright © 2018 Jeremy Dormitzer
;;
;; This file is part of Looped In.
;;
;; Looped In is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Looped In is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Looped In.  If not, see <http://www.gnu.org/licenses/>.

(ns looped-in.hackernews
  (:require [cljs.core.async :refer [go go-loop >! chan close! pipe]]
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

(defn get-max-item-id []
  (let [channel (chan)]
    (GET "https://hacker-news.firebaseio.com/v0/maxitem.json"
         {:handler (fn [res] (go (>! channel res)))
          :error-handler (fn [err]
                           (log/error (str "Error fetching max item: " err))
                           (close! channel))})
    channel))

(defn fetch-items-in-range-helper
  [channel min-id max-id]
  (if (= min-id max-id)
    (do (close! channel)
        channel)
    (let [response-chan (fetch-item min-id)]
      (pipe response-chan channel false)
      (fetch-items-in-range-helper channel (inc min-id) max-id))))

(defn fetch-items-in-range
  "Returns a channel that will contain all items between min-id (inclusive) and max-id (exclusive)."
  [min-id max-id]
  (fetch-items-in-range-helper (chan) min-id max-id))
