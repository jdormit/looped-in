(ns looped-in.sidebar
  (:require [goog.dom :as dom]
            [goog.html.sanitizer.HtmlSanitizer :as Sanitizer]
            [cljs.core.async :refer [go <!]]
            [looped-in.hackernews :as hn]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log])
  (:import (goog.ui Zippy)))

(defn log [& args]
  (let [bg (-> js/browser (.-extension) (.getBackgroundPage))]
    (apply (-> bg (.-console) (.-log)) "[Looped In]" (map clj->js args))))

(defn comment-dom [{:strs [text author children]}]
  (let [$text (dom/createDom "div"
                             #js {:class "commentText body20"}
                             (dom/safeHtmlToNode (Sanitizer/sanitize text)))
        $author (dom/createDom "div"
                              #js {:class "commentAuthor"}
                              author)
        $card (dom/createDom "div" #js {:class "card"} $text $author)]
    (if (> (count children) 0)
      (let [$toggle (dom/createDom "img"
                                   #js {:class "commentToggle"
                                        :src "icons/arrowhead-down-16.svg"
                                        :width "16px"
                                        :height "16px"})
            $children (apply dom/createDom
                             "div"
                             #js {:class "commentChildren"}
                             (clj->js (map comment-dom children)))]
        (log $toggle)
        (Zippy. $toggle $children)
        (dom/appendChild $card $toggle)
        (dom/createDom "div"
                       #js {:class "comment"}
                       $card
                       $children))
      (dom/createDom "div"
                     #js {:class "comment"}
                     $card))))

(defn comments-dom [comments]
  (clj->js
   (apply dom/createDom
          "div"
          #js {:class "comments"}
          (map comment-dom comments))))

(defn story-dom [story]
  (let [$title (dom/createDom "div"
                              #js {:class "storyTitle title20 card"}
                              (story "title"))
        $comments (comments-dom (filter #(= "comment" (% "type")) (story "children")))]
    (Zippy. $title $comments)
    (dom/createDom "div"
                   #js {:class "story"}
                   $title
                   $comments)))

(defn render-items [items]
  (dom/removeChildren (dom/getElement "storiesContainer"))
  (let [stories (filter #(= "story" (% "type")) items)
        $stories (clj->js (map story-dom stories))
        $storiesContainer (dom/getElement "storiesContainer")]
    (log items)
    (dom/append $storiesContainer $stories)))

(defn fetch-and-render-items [ids]
  (go (-> ids
          (hn/fetch-items)
          (<!)
          ((fn [items] (filter #(not (nil? %)) items)))
          (render-items))))

(defn handle-message [msg]
  (case (.-type msg)
    "objectIds" (fetch-and-render-items (.-ids msg))
    (log/error (str "Unknown message type " (.-type msg)))))

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener handle-message))
