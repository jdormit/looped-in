(ns looped-in.background)

(enable-console-print!)

(-> js/browser
    (.-runtime)
    (.-onMessage)
    (.addListener (fn [msg]
                    (-> js/browser
                        (.-browserAction)
                        (.setBadgeText (.-numComments msg))))))
