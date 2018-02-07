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

(ns looped-in.background
  (:require [cljs.core.async :refer [go <!]]
            [ajax.core :refer [GET]]
            [goog.string :as gstring]
            [looped-in.hackernews :as hn]
            [looped-in.logging :as log]
            [looped-in.promises :refer [channel->promise promise->channel]]))

(enable-console-print!)

(def object-ids (atom []))
(def hits (atom []))

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
  "Sorts hits from hn.algolia.com by points date descending"
  [hits]
  (sort-by #(get % "points") #(compare %2 %1) hits))

(defn total-num-comments
  "Returns the total number of comments from some hits"
  [hits]
  (reduce (fn [acc {:strs [num_comments]}] (+ acc num_comments)) 0 hits))

(defn set-badge-text! [text]
  (.setBadgeBackgroundColor (.-browserAction js/browser) #js {:color "#232323"})
  (.setBadgeText (.-browserAction js/browser) #js {:text text}))

(defn handle-tab-update [tab-id]
  (go (let [url (-> js/browser
                    (.-tabs)
                    (.query #js {:active true :currentWindow true})
                    (promise->channel)
                    (<!)
                    (first)
                    (.-url))
            fetched-hits (-> url
                             (hn/fetch-submission)
                             (<!)
                             (filter-response url)
                             (sort-hits))
            ids (map #(% "objectID") fetched-hits)
            num-comments (total-num-comments fetched-hits)]
        (reset! hits fetched-hits)
        (reset! object-ids ids)
        (set-badge-text! (str num-comments)))))

(defn handle-browser-action [tab]
  (-> js/browser
      (.-tabs)
      (.sendMessage (.-id tab)
                    (clj->js {:type "openSidebar"}))))

(defn handle-message [msg sender respond]
  (case (.-type msg)
    "hits" (channel->promise (go @hits))
    "fetchItem" (channel->promise
                 (go (clj->js (<! (hn/fetch-item (.-id msg))))))))

(-> js/browser
    (.-tabs)
    (.-onActivated)
    (.addListener handle-tab-update))

(-> js/browser
    (.-tabs)
    (.-onUpdated)
    (.addListener handle-tab-update))

(-> js/browser
    (.-browserAction)
    (.-onClicked)
    (.addListener handle-browser-action))

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener handle-message))
