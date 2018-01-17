(ns looped-in.content
  (:require [goog.dom :as dom]
            [goog.style :as style]
            [looped-in.logging :as log]))

(defn sidebar-dom []
  (let [sidebar-url (-> js/browser (.-extension) (.getURL "sidebar.html"))]
    (dom/createDom "iframe"
                   #js {:src sidebar-url
                        :style (style/toStyleAttribute
                                #js {:position "fixed"
                                     :height "100%"
                                     :width "300px"
                                     :top 0
                                     :left 0})})))

(defn open-sidebar []
  (dom/appendChild js/document.body (sidebar-dom)))

(defn handle-message [msg]
  (case (.-type msg)
    "openSidebar" (open-sidebar)))

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener handle-message))
