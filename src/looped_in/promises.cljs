(ns looped-in.promises
  (:require [cljs.core.async :refer [go chan >! <!]]
            [looped-in.logging :as log]))

(defn channel->promise
  "Returns a Promise that resolves by taking from the channel"
  [channel]
  (js/Promise. (fn [resolve]
                 (go (let [result (clj->js (<! channel))]
                       (resolve result))))))

(defn promise->channel
  "Returns a channel that is put into when the Promise resolves"
  [p]
  (let [channel (chan)]
    (-> p
        (.then (fn [result]
                 (go (>! channel result))))
        (.catch (fn [err]
                (log/error "Error resolving Promise:" err))))
    channel))
