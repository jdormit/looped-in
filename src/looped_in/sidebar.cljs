(ns looped-in.sidebar
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [goog.html.sanitizer.HtmlSanitizer :as Sanitizer]
            [cljs.core.async :refer [go <!]]
            [looped-in.hackernews :as hn]
            [looped-in.components :as components]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log])
  (:require-macros [looped-in.macros :refer [get-in-item]])
  (:import (goog.ui Zippy)))

(enable-console-print!)

(defn obj->clj [obj]
  (into {} (for [k (.keys js/Object obj)]
             [(keyword k)
              (let [v (aget obj k)]
                (cond
                  (object? v) (obj->clj v)
                  (.isArray js/Array v) (map obj->clj (array-seq v))
                  :default (js->clj v)))])))

(defn fetch-item
  "Fetch the item with id `id`"
  [id]
  (go (-> js/browser
          (.-runtime)
          (.sendMessage (clj->js {:type "fetchItem"
                                  :id id}))
          (promise->channel)
          (<!)
          (obj->clj))))

(defn model
  "Returns initial sidebar state"
  []
  {:item nil
   :hits nil
   :depth []
   :loading false})

(defn update-state
  "Given a message and the old state, returns the new state"
  [msg state]
  (case (:type msg)
    :got-item (-> state
                  (assoc :item (:item msg))
                  (assoc :loading false))
    :got-hits (-> state
                  (assoc :hits (:hits msg))
                  (assoc :loading false))
    :loading (assoc state :loading (:loading msg))
    state))

(defn view
  "Given a callback to dispatch an update message and the sidebar state, returns the sidebar DOM"
  [dispatch-message state]
  (log/debug state)
  (cond
    (:loading state) (components/loader)
    (:item state) (let [current-item (get-in-item (:item state) (:depth state))]
                    (cons
                     (case (:type current-item)
                       "story" (components/card
                                (components/body30 (:title current-item))
                                (components/story-caption (:points current-item)
                                                          (:author current-item)
                                                          (* (:created_at_i current-item) 1000)))
                       "comment" ())
                     (map (fn [child]
                            (-> (components/card
                                 (components/comment-caption (:author child)
                                                             (* (:created_at_i child) 1000))
                                 (components/comment-text (:text child))
                                 (components/replies-indicator (count (:children child))))
                                (components/with-classes "child")))
                          (->> (:children current-item)
                               (filter #(contains? % :text))
                               (sort-by #(count (:children %)) #(compare %2 %1))))))
    (:hits state) (map (fn [hit]
                         (-> (components/card
                              (components/body30 (:title hit))
                              (components/story-caption (:points hit)
                                                        (:author hit)
                                                        (* (:created_at_i hit) 1000))
                              (components/comments-indicator (:num_comments hit)))
                             ((fn [card]
                                (if (> (:num_comments hit) 0)
                                  (components/with-classes card "clickable")
                                  card)))
                             (components/with-listener
                               "click"
                               (fn [e]
                                 (dispatch-message {:type :loading :loading true})
                                 (go
                                   (-> (fetch-item (:objectID hit))
                                       (<!)
                                       ((fn [item]
                                          (dispatch-message {:type :got-item :item item})))))))))
                       (:hits state))
    #_(let [current-item (get-in-items (:items state) (:depth state))]
      (if (> (count current-item) 1)
        (map #(components/card (:title %)) current-item)
        ()))))

(defn render
  "Renders the new DOM

  This is where clever diffing algorithms would go if this was React"
  [$sidebar-dom]
  (let [$container (dom/getElement "sidebarContent")]
    (dom/removeChildren $container)
    (if (seqable? $sidebar-dom)
      (apply dom/append $container $sidebar-dom)
      (dom/append $container $sidebar-dom))))

(defn run-render-loop
  "Runs the model-update-view loop"
  [state]
  (let [dispatch-message (fn [msg]
                           (let [new-state (update-state msg state)]
                             (run-render-loop new-state)))]
    (render (view dispatch-message state))))

(defn handle-close-button [e]
  (.postMessage js/window.parent (clj->js {:type "closeSidebar"}) "*"))

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
  (events/listen (dom/getElement "closeSidebar") "click" handle-close-button)
  (let [initial-state (update-state {:type :loading :loading true} (model))]
    (run-render-loop initial-state)
    (go (-> (fetch-hits)
            (<!)
            (#(update-state {:type :got-hits
                             :hits %} initial-state))
            (run-render-loop)))))

(init)
