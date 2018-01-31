(ns looped-in.components
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.events :as events]
            [goog.object :as gobject]
            [goog.html.sanitizer.HtmlSanitizer :as Sanitizer]
            [clojure.string :as string]
            [looped-in.logging :as log])
  (:import (goog.date DateTime)))

(def moment (-> (gobject/get js/window "moment-range")
                (.extendMoment js/moment)))

(defn card [& children]
  (apply dom/createDom "div" "card" children))

(defn body30 [text]
  (dom/createDom "span" "body30" text))

(defn caption30 [text]
  (dom/createDom "span" "caption30" text))

(defn with-append [el child]
  (dom/appendChild el child)
  el)

(defn sidebar-content [& body]
  (apply dom/createDom "div" (clj->js {:id "sidebarContent"}) body))

(defn comments-indicator [num-comments]
  (let [text (caption30 (str num-comments " comment" (when (not= num-comments 1) "s")))]
    (dom/createDom "div"
                   "commentsIndicator"
                   (if (> num-comments 0)
                     (with-append
                       text
                       (dom/createDom
                        "img"
                        (clj->js
                         {:src "icons/arrowhead-down-16.svg"
                          :height "16px"
                          :width "16px"
                          :class "clickableArrowIcon"})))
                     text))))

(defn replies-indicator [num-replies]
  (let [text (caption30 (str num-replies " " (if (not= num-replies 1) "replies" "reply")))]
    (dom/createDom "div"
                   "commentsIndicator"
                   (if (> num-replies 0)
                     (with-append
                       text
                       (dom/createDom
                        "img"
                        (clj->js
                         {:src "icons/arrowhead-down-16.svg"
                          :height "16px"
                          :width "16px"
                          :class "clickableArrowIcon"})))
                     text))))

(defn with-listener [el type listener]
  (events/listen el type listener)
  el)

(defn item-link [id]
  (with-listener
    (dom/createDom "a"
                   (clj->js {:class "itemLink"
                             :href (str "https://news.ycombinator.com/item?id=" id)
                             :target "_blank"})
                   (dom/createDom "img"
                                  (clj->js {:src "icons/open-in-new-16.svg"
                                            :width 12
                                            :height 12})))
    "click"
    #(.stopPropagation %)))

(defn get-time-ago-str
  "Returns the string '<number> <unit>' based on how long ago `timestamp` was,
  for example '3 days' or '5 hours'"
  [timestamp]
  (let [range (.range moment (moment timestamp) (moment))
        years (.diff range "years")]
    (if (> years 0)
      (str years " year" (when (not= years 1) "s"))
      (let [days (.diff range "days")]
        (if (> days 0)
          (str days " day" (when (not= days 1) "s"))
          (let [hours (.diff range "hours")]
            (str hours " hour" (when (not= days 1) "s"))))))))

(defn comment-text [text]
  (dom/createDom "div" "body10" (dom/safeHtmlToNode (Sanitizer/sanitize text))))

(defn story-caption [points author timestamp]
  (dom/createDom "div"
                 "storyCaption caption10"
                 (str points " points by " author " " (get-time-ago-str timestamp) " ago")))

(defn comment-caption [author timestamp]
  (dom/createDom "div"
                 "caption10"
                 (str author " " (get-time-ago-str timestamp) " ago")))

(defn loader []
  (apply dom/createDom "div" "spinner"
         (for [i (range 1 6)]
           (dom/createDom "div" (str "rect" i)))))

(defn sidebar-header [icon-src]
  (dom/createDom "div" "sidebarHeader"
                 (dom/createDom "img"
                                (clj->js {:class "headerIcon"
                                          :src icon-src
                                          :width "16px"
                                          :height "16px"}))
                 (dom/createDom "span" "body30 headerTitle" "Looped In")
                 (dom/createDom "button"
                                (clj->js {:class "iconButton"
                                          :id "closeSidebar"})
                                (dom/createDom "img"
                                               (clj->js {:src "icons/stop-16.svg"
                                                         :width "16px"
                                                         :height "16px"})))))

(defn with-classes [el & classes]
  (doseq [class (filter #(not (string/blank? %)) classes)]
    (classes/add el class))
  el)
