(ns looped-in.components
  (:require [goog.dom :as dom]
            [looped-in.logging :as log]))

(defn card [& children]
  (apply dom/createDom "div" "card" children))

(defn loader []
  (apply dom/createDom "div" "spinner"
         (for [i (range 1 6)]
           (dom/createDom "div" (str "rect" i)))))
