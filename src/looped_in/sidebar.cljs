(ns looped-in.sidebar
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.html.sanitizer.HtmlSanitizer :as Sanitizer]
            [cljs.core.async :refer [go <!]]
            [looped-in.hackernews :as hn]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log])
  (:import (goog.ui Zippy)))

(enable-console-print!)

(defn comment-dom [comment]
  (let [text (.-text comment)
        author (.-author comment)
        children (array-seq (.-children comment))
        $text (dom/createDom "div"
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
                              (.-title story))
        $comments (comments-dom (filter #(= "comment" (.-type %)) (array-seq (.-children story))))]
    (Zippy. $title $comments)
    (dom/createDom "div"
                   #js {:class "story"}
                   $title
                   $comments)))

(defn render-items [items]
  #_(dom/removeChildren (dom/getElement "sidebarContent"))
  #_(let [stories (filter #(= "story" (.-type %)) items)
        $stories (clj->js (map story-dom stories))
        $storiesContainer (dom/getElement "sidebarContent")]
    (dom/append $storiesContainer $stories)))

(defn handle-close-button [e]
  (.postMessage js/window.parent (clj->js {:type "closeSidebar"}) "*"))

(go (-> js/browser
        (.-runtime)
        (.sendMessage (clj->js {:type "fetchItems"}))
        (promise->channel)
        (<!)
        (array-seq)
        ((fn [items] (filter #(not (nil? %)) items)))
        (render-items)))

(events/listen (dom/getElement "closeSidebar") "click" handle-close-button)
