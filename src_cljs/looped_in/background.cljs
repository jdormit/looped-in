(ns looped-in.background)

(enable-console-print!)

(def browser-action (.-browserAction js/browser))

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener (fn [msg]
                    (prn msg)
                    (.setBadgeBackgroundColor browser-action #js {:color "#232323"})
                    (.setBadgeText browser-action
                                   #js {:text (str (.-numComments msg))}))))
