(ns looped-in.sidebar
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [go <!]]
            [looped-in.hackernews :as hn]
            [looped-in.components :as components]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log]))

(enable-console-print!)

(defn obj->clj [obj]
  (into {} (for [k (.keys js/Object obj)]
             [(keyword k)
              (let [v (aget obj k)]
                (cond
                  (object? v) (obj->clj v)
                  (.isArray js/Array v) (map obj->clj (array-seq v))
                  :default (js->clj v)))])))

(defn sort-and-filter-item
  [item]
  (assoc item :children
         (->> (:children item)
              (filter #(contains? % :text))
              (sort-by #(count (:children % [])) #(compare %2 %1))
              (map (fn [child]
                     (if (> (count (:children child)) 0)
                       (sort-and-filter-item child)
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
          (sort-and-filter-item))))

(defn get-in-item
  "Retrieves the child from item represented by `depth`"
  [item [fdepth & rdepth]]
  (if (nil? fdepth)
    item
    (get-in-item (nth (:children item) fdepth) rdepth)))

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
    :enq-depth (assoc state :depth (conj (:depth state) (:index msg)))
    :loading (assoc state :loading (:loading msg))
    state))

(defn view
  "Given a callback to dispatch an update message and the sidebar state, returns the sidebar DOM"
  [dispatch-message state]
  (log/debug state)
  (cond
    (:loading state) (list (components/sidebar-header "icons/icon48.png")
                           (components/sidebar-content (components/loader)))
    (:item state) (list
                   (components/sidebar-header "icons/back-16.svg")
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
                                           (-> (components/replies-indicator (count (:children child)))
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
                                    (->> (:children current-item)
                                         (filter #(contains? % :text))
                                         (sort-by #(count (:children %)) #(compare %2 %1))))))))
    (:hits state) (list
                   (components/sidebar-header "icons/icon48.png")
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
  (events/listen (dom/getElement "closeSidebar") "click" handle-close-button))

(defn run-render-loop
  "Runs the model-update-view loop"
  [state]
  (let [dispatch-message (fn [msg]
                           (let [new-state (update-state msg state)]
                             (run-render-loop new-state)))]
    (render (view dispatch-message state))
    (handle-events state)))

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
  (let [initial-state (update-state {:type :loading :loading true} (model))]
    (run-render-loop initial-state)
    (go (-> (fetch-hits)
            (<!)
            (#(update-state {:type :got-hits
                             :hits %} initial-state))
            (run-render-loop)))))

(init)
