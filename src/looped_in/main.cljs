(ns looped-in.main
  (:require [clojure.core.match :refer [match]]
            [cljs.core.async :refer [go chan >! <!]]
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
          :error-handler (fn [err] (log/error "Error fetching HN stories:"
                                              (clj->js err)))})
    response-chan))

(defn url-path [url]
  (when (not (nil? url))
    (second (re-matches #"http.*://(.*)" url))))

(defn filter-response
  "Filters a response from hn.algolia.com to give just the relevant results"
  [url response]
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
  (go (let [query-p (-> js/browser (.-tabs) (.query #js {:active true
                                                      :currentWindow true}))
            query-chan (promise->channel query-p)
            tab (first (<! query-chan))
            url (.-url tab)
            fetch-chan (fetch-submission url)
            response (<! fetch-chan)
            hits (->> response
                      (filter-response url)
                      (sort-hits))
            num-comments (total-num-comments hits)]
        (set-badge-text! (str num-comments)))))

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
