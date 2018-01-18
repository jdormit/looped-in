(ns looped-in.content
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.style :as style]
            [looped-in.logging :as log]))

(def sidebar-width 300)

(defn sidebar-dom []
  (let [sidebar-url (-> js/browser (.-extension) (.getURL "sidebar.html"))]
    (dom/createDom "iframe"
                   #js {:src sidebar-url
                        :id "loopedInSidebar"
                        :style (style/toStyleAttribute
                                #js {:position "fixed"
                                     :height "100%"
                                     :border 0
                                     :borderRight "2px solid #d7d7db"
                                     :zIndex 9999
                                     :width (str sidebar-width "px")
                                     :top 0
                                     :left 0})})))

(def old-html-padding (atom ""))

(defn open-sidebar []
  (let [$html (.-documentElement js/document)
        $body js/document.body
        $sidebar (sidebar-dom)]
    (reset! old-html-padding (-> $html (.-style) (.-paddingLeft)))
    (set! (-> $html (.-style) (.-paddingLeft)) (str (+ sidebar-width 12) "px"))
    (dom/appendChild $body $sidebar)))

(defn close-sidebar []
  (let [$html (.-documentElement js/document)
        $sidebar (dom/getElement "loopedInSidebar")]
    (dom/removeNode $sidebar)
    (set! (-> $html (.-style) (.-paddingLeft)) @old-html-padding)))

(defn handle-runtime-message [msg]
  (case (.-type msg)
    "openSidebar" (open-sidebar)))

(defn handle-window-message [e]
  (let [msg (.-data (.getBrowserEvent e))]
    (case (.-type msg)
     "closeSidebar" (close-sidebar))))

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener handle-runtime-message))

(events/listen js/window "message" handle-window-message)
