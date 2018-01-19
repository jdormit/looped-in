(ns looped-in.components
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.events :as events]
            [goog.object :as gobject]
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

(defn comments-indicator [num-comments]
  (dom/createDom "div"
                 "commentsIndicator"
                 (caption30 (str num-comments " comment" (when (not= num-comments 1) "s")))))

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

(defn story-caption [points author timestamp]
  (dom/createDom "div"
                 "storyCaption caption10"
                 (str points " points by " author " " (get-time-ago-str timestamp) " ago")))

(defn loader []
  (apply dom/createDom "div" "spinner"
         (for [i (range 1 6)]
           (dom/createDom "div" (str "rect" i)))))

(defn with-listener [el type listener]
  (events/listen el type listener)
  el)

(defn with-classes [el & classes]
  (doseq [class (filter #(not (string/blank? %)) classes)]
    (classes/add el class))
  el)
