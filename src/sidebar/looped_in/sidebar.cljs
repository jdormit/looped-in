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

(ns looped-in.sidebar
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [go <!]]
            [looped-in.hackernews :as hn]
            [looped-in.components :as components]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log]
            [clojure.string :as string]))

(enable-console-print!)

(defn obj->clj [obj]
  (into {} (for [k (.keys js/Object obj)]
             [(keyword k)
              (let [v (aget obj k)]
                (cond
                  (object? v) (obj->clj v)
                  (.isArray js/Array v) (map obj->clj (array-seq v))
                  :default (js->clj v)))])))

(defn filter-item [item]
  (assoc item :children
         (->> (:children item)
              (filter #(and (contains? % :text)
                            (not (string/blank? (:text %)))))
              (map (fn [child]
                     (if (> (count (:children child)) 0)
                       (filter-item child)
                       child)))
              (vec))))

(defn get-total-children-count [item]
  (if (= (count (:children item)) 0)
    0
    (apply +
           (count (:children item))
           (map get-total-children-count (:children item)))))

(defn sort-item [item]
  (assoc item :children
         (->> (:children item)
              (sort-by get-total-children-count #(compare %2 %1))
              (map (fn [child]
                     (if (> (count (:children child)) 0)
                       (sort-item child)
                       child)))
              (vec))))

(defn fetch-item
  "Fetch the item with id `id`"
  [id]
  (go (-> js/browser
          (.-runtime)
          (.sendMessage (clj->js {:type "fetchItem"
                                  :id id}))
          (promise->channel)
          (<!)
          (obj->clj)
          (filter-item)
          (sort-item))))

(defn get-in-item
  "Retrieves the child from item represented by `depth`"
  [item [fdepth & rdepth]]
  (if (nil? fdepth)
    item
    (get-in-item (nth (:children item) fdepth) rdepth)))

(defonce state
  (atom {:item nil
         :hits nil
         :depth []
         :loading false}))

(defn update-state
  "Given a message, returns the new state"
  [msg]
  (case (:type msg)
    :got-item (swap! state
                     #(-> %
                          (assoc :item (:item msg))
                          (assoc :loading false)))
    :got-hits (swap! state
                     #(-> %
                          (assoc :hits (:hits msg))
                          (assoc :loading false)))
    :enq-depth (swap! state
                      #(assoc % :depth (conj (:depth @state) (:index msg))))
    :deq-depth (swap! state
                      #(assoc % :depth
                              (subvec (:depth @state) 0 (- (count (:depth @state)) 1))))
    :clear-item (swap! state
                       #(assoc % :item nil))
    :loading (swap! state
                    #(assoc % :loading (:loading msg)))
    @state))

(declare dispatch-message)

(defn view
  "Given a callback to dispatch an update message and the sidebar state, returns the sidebar DOM"
  [state]
  (cond
    (:loading state) (list (components/sidebar-header (components/with-classes
                                                        (components/header-icon "icons/icon48.png")
                                                        "headerIcon" "nonClickable"))
                           (components/sidebar-content (components/loader)))
    (:item state) (list
                   (components/sidebar-header (dom/createDom
                                               "button"
                                               (clj->js {:class "headerIcon iconButton"
                                                         :id "backButton"})
                                               (components/header-icon "icons/back-16.svg")))
                   (apply
                    components/sidebar-content
                    (let [current-item (get-in-item
                                        (:item state)
                                        (:depth state))]
                      (cons
                       (case (:type current-item)
                         "story" (components/card
                                  (dom/createDom
                                   "div"
                                   "storyHeader"
                                   (components/body30 (:title current-item))
                                   (components/item-link (:id current-item)))
                                  (components/story-caption (:points current-item)
                                                            (:author current-item)
                                                            (* (:created_at_i current-item) 1000)))
                         "comment" (components/card
                                    (dom/createDom
                                     "div"
                                     "commentHeader"
                                     (components/comment-caption (:author current-item)
                                                                 (* (:created_at_i current-item)
                                                                    1000))
                                     (components/item-link (:id current-item)))
                                    (components/comment-text (:text current-item))))
                       (map-indexed (fn [index child]
                                      (-> (components/card
                                           (dom/createDom
                                            "div"
                                            "commentHeader"
                                            (components/comment-caption
                                             (:author child)
                                             (* (:created_at_i child) 1000))
                                            (components/item-link (:id child)))
                                           (components/comment-text (:text child))
                                           (-> (components/replies-indicator
                                                (count (:children child)))
                                               ((fn [indicator]
                                                  (if (> (count (:children child)) 0)
                                                    (-> indicator
                                                        (components/with-classes "clickable")
                                                        (components/with-listener
                                                          "click"
                                                          (fn [e]
                                                            (dispatch-message
                                                             {:type :enq-depth
                                                              :index index}))))
                                                    indicator)))))
                                          (components/with-classes "child")
                                          ((fn [card]
                                             (if (> (count (:children child)) 0)
                                               (components/with-classes card "clickable")
                                               card)))))
                                    (:children current-item))))))
    (:hits state) (list
                   (components/sidebar-header (components/with-classes
                                                (components/header-icon "icons/icon48.png")
                                                "headerIcon" "nonClickable"))
                   (apply
                    components/sidebar-content
                    (map (fn [hit]
                           (-> (components/card
                                (dom/createDom
                                 "div"
                                 "storyHeader"
                                 (components/body30 (:title hit))
                                 (components/item-link (:objectID hit)))
                                (components/story-caption (:points hit)
                                                          (:author hit)
                                                          (* (:created_at_i hit) 1000))
                                (-> (components/comments-indicator (:num_comments hit))
                                    ((fn [indicator]
                                       (if (> (:num_comments hit) 0)
                                         (-> indicator
                                             (components/with-classes "clickable")
                                             (components/with-listener
                                               "click"
                                               (fn [e]
                                                 (dispatch-message {:type :loading :loading true})
                                                 (go
                                                   (-> (fetch-item (:objectID hit))
                                                       (<!)
                                                       ((fn [item]
                                                          (dispatch-message
                                                           {:type :got-item :item item}))))))))
                                         indicator)))))
                               ((fn [card]
                                  (if (> (:num_comments hit) 0)
                                    (components/with-classes card "clickable")
                                    card)))))
                         (:hits state))))))

(defn render
  "Renders the new DOM

  This is where clever diffing algorithms would go if this was React"
  [$sidebar-dom]
  (let [$container (dom/getElement "sidebar")]
    (dom/removeChildren $container)
    (.scrollTo js/window 0 0)
    (if (seqable? $sidebar-dom)
      (apply dom/append $container $sidebar-dom)
      (dom/append $container $sidebar-dom))))

(defn handle-close-button [e]
  (.postMessage js/window.parent (clj->js {:type "closeSidebar"}) "*"))

(defn handle-events
  "Registers event listeners"
  [state]
  (events/listen (dom/getElement "closeSidebar") "click" handle-close-button)
  (when (not (nil? (dom/getElement "backButton")))
    (events/listen (dom/getElement "backButton")
                   "click"
                   (fn [e]
                     (let [depth (:depth state)]
                       (if (> (count depth) 0)
                         (dispatch-message {:type :deq-depth})
                         (dispatch-message
                          {:type :clear-item})))))))

(declare run-render-loop)

(defn dispatch-message
  "Dispatches a message to change the state"
  [msg]
  (let [new-state (update-state msg)]
    (run-render-loop new-state)))

(defn run-render-loop
  "Runs the model-update-view loop"
  [state]
  (render (view state))
  (handle-events state))

(defn fetch-hits
  "Fetch hits in the Algolia API matching the URL"
  []
  (go (-> js/browser
          (.-runtime)
          (.sendMessage (clj->js {:type "hits"}))
          (promise->channel)
          (<!)
          (array-seq)
          ((fn [hits] (map obj->clj hits))))))

(defn init
  "Initializes the sidebar"
  []
  (let [initial-state (update-state {:type :loading :loading true})]
    (run-render-loop initial-state)
    (go (-> (fetch-hits)
            (<!)
            (#(update-state {:type :got-hits
                             :hits %}))
            (run-render-loop)))))

(init)
