(ns looped-in.components
  (:require [goog.dom :as dom]
            [looped-in.logging :as log]))

(defn card [& children]
  (apply dom/createDom "div" "card" children))
