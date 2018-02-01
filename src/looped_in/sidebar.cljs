(ns looped-in.sidebar
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :refer [go <!]]
            [looped-in.hackernews :as hn]
            [looped-in.components :as components]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log]
            [looped-in.analytics :as analytics]))

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
    :deq-depth (assoc state :depth
                      (subvec (:depth state) 0 (- (count (:depth state)) 1)))
    :clear-item (assoc state :item nil)
    :loading (assoc state :loading (:loading msg))
    state))

(defn view
  "Given a callback to dispatch an update message and the sidebar state, returns the sidebar DOM"
  [dispatch-message state]
  (log/debug state)
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
                                   (components/with-listener
                                     (components/item-link (:id current-item))
                                     "click"
                                     (fn [e]
                                       (analytics/log-event
                                        "CLICKED_HN_EXTERNAL_LINK"
                                        {:type "story"}))))
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
                                     (components/with-listener
                                       (components/item-link (:id current-item))
                                       "click"
                                       (fn [e]
                                         (analytics/log-event
                                          "CLICKED_HN_EXTERNAL_LINK"
                                          {:type "comment"}))))
                                    (components/comment-text (:text current-item))))
                       (map-indexed (fn [index child]
                                      (-> (components/card
                                           (dom/createDom
                                            "div"
                                            "commentHeader"
                                            (components/comment-caption
                                             (:author child)
                                             (* (:created_at_i child) 1000))
                                            (components/with-listener
                                              (components/item-link (:id child))
                                              "click"
                                              (fn [e]
                                                (analytics/log-event
                                                 "CLICKED_HN_EXTERNAL_LINK"
                                                 {:type "comment"}))))
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
                                                            (analytics/log-event
                                                             "CLICKED_HN_VIEW_COMMENTS"
                                                             {:depth (count (:depth state))})
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
                                 (components/with-listener
                                   (components/item-link (:objectID hit))
                                   "click"
                                   (fn [e]
                                     (analytics/log-event
                                      "CLICKED_HN_EXTERNAL_LINK"
                                      {:type "story"}))))
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
                                                 (analytics/log-event "CLICKED_HN_STORY")
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
  (go (-> (analytics/log-event "CLOSED_SIDEBAR")
          (<!)
          ((fn [_]
             (.postMessage js/window.parent (clj->js {:type "closeSidebar"}) "*"))))))

(defn handle-events
  "Registers event listeners"
  [dispatch-message state]
  (events/listen (dom/getElement "closeSidebar") "click" handle-close-button)
  (when (not (nil? (dom/getElement "backButton")))
    (events/listen (dom/getElement "backButton")
                   "click"
                   (fn [e]
                     (analytics/log-event "CLICKED_BACK_BUTTON" {:depth (count (:depth state))})
                     (let [depth (:depth state)]
                       (if (> (count depth) 0)
                         (dispatch-message {:type :deq-depth})
                         (dispatch-message
                          {:type :clear-item})))))))

(defn run-render-loop
  "Runs the model-update-view loop"
  [state]
  (let [dispatch-message (fn [msg]
                           (let [new-state (update-state msg state)]
                             (run-render-loop new-state)))]
    (render (view dispatch-message state))
    (handle-events dispatch-message state)))

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
  (analytics/init-amplitude)
  (analytics/log-event "OPENED_SIDEBAR")
  (let [initial-state (update-state {:type :loading :loading true} (model))]
    (run-render-loop initial-state)
    (go (-> (fetch-hits)
            (<!)
            (#(update-state {:type :got-hits
                             :hits %} initial-state))
            (run-render-loop)))))

(init)
