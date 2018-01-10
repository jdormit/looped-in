(ns looped-in.content
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ajax.core :refer [GET]]
            [cljs.core.async :as async :refer [chan <! >!]]
            [looped-in.logging :as log]))

(enable-console-print!)

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

(defn filter-response
  "Filters a response from hn.algolia.com to give just the relevant results"
  ;; TODO implement a cache by URL and timestamp that caches the results of the
  ;; request and this method for 5 minutes using localStorage
  [url response]
  (let [{:strs [hits]} response]
    (log/debug "response" response)
    (filter #(= (get % "url") url) hits)))

(defn sort-hits
  "Sorts hits from hn.algolia.com by post date descending"
  [hits]
  (sort-by #(get % "created_at_i") #(compare %2 %1) hits))

(defn total-num-comments
  "Returns the total number of comments from some hits"
  [hits]
  (reduce (fn [acc {:strs [num_comments]}] (+ acc num_comments)) 0 hits))

(defn handle-hits
  "Handles a filtered response"
  [hits]
  (log/debug (clj->js hits))
  (let [num-comments (total-num-comments hits)]
    (-> js/browser
        (.-runtime)
        (.sendMessage #js {:numComments num-comments}))))

(let [current-url (-> js/window (.-location) (.-href))
      sub-chan (fetch-submission current-url)]
  (go (->> (<! sub-chan)
           (filter-response current-url)
           (sort-hits)
           (handle-hits))))
