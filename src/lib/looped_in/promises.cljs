;; Copyright © 2018 Jeremy Dormitzer
;;
;; This file is part of Looped In.
;;
;; Looped In is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Looped In is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Looped In.  If not, see <http://www.gnu.org/licenses/>.

(ns looped-in.promises
  (:require [cljs.core.async :refer [go chan close! >! <!]]
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
                  (log/error "Error resolving Promise:" err)
                  (close! channel))))
    channel))
