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

(defn model
  "Returns initial sidebar state"
  []
  {:items ()})

(defn update-state
  "Given a message and the old state, returns the new state"
  [msg state]
  (case (:type msg)
    :items (assoc state :items (:items msg))
    state))

(defn view
  "Given a callback to dispatch an update message and the sidebar state, returns the sidebar DOM"
  [dispatch-message state]
  ())

(defn render
  "Renders the new DOM

  This is where clever diffing algorithms would go if this was React"
  [$sidebar-dom]
  (let [$container (dom/getElement "sidebarContent")]
    (dom/removeChildren $container)
    (apply dom/append $container $sidebar-dom)))

(defn main
  "Runs the model-update-view loop"
  [state]
  (let [dispatch-message (fn [msg]
                           (let [new-state (update-state msg state)]
                             (main new-state)))]
    (render (view dispatch-message state))))

(main (model))

(defn obj->clj [obj]
  (into {} (for [k (.keys js/Object obj)]
             [(keyword k)
              (let [v (aget obj k)]
                (cond
                  (object? v) (obj->clj v)
                  (.isArray js/Array v) (map obj->clj (array-seq v))
                  :default (js->clj v)))])))

(go (-> js/browser
        (.-runtime)
        (.sendMessage (clj->js {:type "fetchItems"}))
        (promise->channel)
        (<!)
        (array-seq)
        ((fn [items] (filter #(not (nil? %)) items)))
        (#(update-state {:type :items
                   :items (map obj->clj %)} (model)))
        (main)))

(defn handle-close-button [e]
  (.postMessage js/window.parent (clj->js {:type "closeSidebar"}) "*"))

(events/listen (dom/getElement "closeSidebar") "click" handle-close-button)
