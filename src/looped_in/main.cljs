(ns looped-in.main
  (:require [clojure.core.match :refer [match]]
            [cljs.core.async :refer [go go-loop chan close! >! <!]]
            [ajax.core :refer [GET]]
            [looped-in.logging :as log]
            [looped-in.promises :refer [channel->promise promise->channel]]))

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

(defn fetch-items-for-hits [hits]
  (let [chans (map (fn [hit]
                     (prn (hit "objectID"))
                     (fetch-item (hit "objectID")))
                   hits)]
    (go-loop [[channel & rest] chans
              acc []]
      (if (nil? channel)
        acc
        (recur rest (conj acc (<! channel)))))))

(defn url-path
  "Returns a url without its protocol"
  [url]
  (when (not (nil? url))
    (second (re-matches #"http.*://(.*)" url))))

(defn filter-response
  "Filters a response from hn.algolia.com to give just the relevant results"
  [response url]
  (let [{:strs [hits]} response]
    (filter #(= (url-path (get % "url")) (url-path url)) hits)))

(defn sort-hits
  "Sorts hits from hn.algolia.com by post date descending"
  [hits]
  (sort-by #(get % "created_at_i") #(compare %2 %1) hits))

(defn total-num-comments
  "Returns the total number of comments from some hits"
  [hits]
  (reduce (fn [acc {:strs [num_comments]}] (+ acc num_comments)) 0 hits))

(defn set-badge-text! [text]
  (.setBadgeBackgroundColor (.-browserAction js/browser) #js {:color "#232323"})
  (.setBadgeText (.-browserAction js/browser) #js {:text text}))

;; TODO implement a cache on the result of the urlVisited message

(defn handle-update [tab-id]
  (go (let [url (-> js/browser
                    (.-tabs)
                    (.query #js {:active true :currentWindow true})
                    (promise->channel)
                    (<!)
                    (first)
                    (.-url))
            hits (-> url
                     (fetch-submission)
                     (<!)
                     (filter-response url)
                     (sort-hits))
            items (<! (fetch-items-for-hits hits))
            num-comments (total-num-comments hits)]
        (set-badge-text! (str num-comments))
        (log/debug items))))

(-> js/browser
    (.-tabs)
    (.-onActivated)
    (.addListener handle-update))

(-> js/browser
    (.-tabs)
    (.-onUpdated)
    (.addListener handle-update))

;; Application logic:
;; 1. Event comes in (new url)
;; 2. Fetch HN hits
;; 3. Filter and sort hits
;; 4. Count comments, update badge
;; 5. Fetch item details
;; 6. Construct popup (or sidebar?) html
