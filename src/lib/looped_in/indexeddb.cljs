(ns looped-in.indexeddb
  (:require [cljs.core.async :refer [go chan >! close!]]
   [looped-in.logging :as log]))

(defn add-data
  "Adds data to IndexedDB"
  [db object-store data]
  (let [channel (chan)
        transaction (.transaction db (array object-store) "readwrite")
        store (.objectStore transaction object-store)
        data-coll (if (coll? data) data [data])]
    (doseq [datum data-coll]
      (.add store datum))
    (set! (.-onerror transaction)
          (fn [event] (log/error "Error adding data to IndexedDB: "
                                 (.-message (.-error (.-target event))))))
    (set! (.-oncomplete transaction)
          (fn [event] (close! channel)))
    channel))

(defn get-data
  "Gets data from IndexedDB."
  [db object-store index]
  (let [channel (chan)
        request (-> db
                    (.transaction (array object-store))
                    (.objectStore object-store)
                    (.get index))]
    (set! (.-onerror request)
          (fn [event] (log/error "Error getting data from IndexedDB: "
                                 (.-message (.-error (.-target event))))))
    (set! (.-onsuccess request)
          (fn [event] (let [res (.-result (.-target event))]
                        (if (nil? res) (close! channel) (go (>! channel res))))))
    channel))

(defn search-index
  "Returns a channel of all data objects matching the index query.
  The channel will contain multiple values if there are multiple matches."
  [db object-store index query]
  (let [channel (chan)
        request (-> db
                    (.transaction (array object-store))
                    (.objectStore object-store)
                    (.index index)
                    (.openCursor query))]
    (set! (.-onerror request)
          (fn [event] (log/error "Error opening cursor into IndexedDB: "
                                 (.-message (.-error (.-target event))))))
    (set! (.-onsuccess request)
          (fn [event]
            (let [cursor (.-result (.-target event))]
              (if (nil? cursor)
                (close! channel)
                (do (go (>! channel (.-value cursor)))
                    (.continue cursor))))))
    channel))

(defn update-data
  "Updates data in IndexedDB.
  `updated-values` is map of key-value pairs to update. It does not need to contain
  every field in the object schema, just those to update."
  [db object-store index updated-values]
  (let [channel (chan)
        store (-> db
                  (.transaction (array object-store) "readwrite")
                  (.objectStore object-store))
        get-request (.get store index)]
    (set! (.-onerror get-request)
          (fn [event] (log/error "Error getting data from IndexedDB: "
                                 (.-message (.-error (.-target event))))))
    (set! (.-onsuccess get-request)
          (fn [event]
            (let [data (js->clj (.-result (.-target event)) :keywordize-keys true)
                  update-request (.put store (clj->js (merge data updated-values)))]
              (set! (.-onerror update-request)
                    (fn [event] (log/error "Error updating data in IndexedDB: "
                                           (.-message (.-error (.-target event))))))
              (set! (.-onsuccess update-request)
                    (fn [event] (go (>! channel (.-result (.-target event)))))))))
    channel))

(defn delete-data
  "Deletes data from IndexedDB"
  [db object-store index]
  (let [channel (chan)
        request (-> db
                    (.transaction (array object-store) "readwrite")
                    (.objectStore object-store)
                    (.delete index))]
    (set! (.-onerror request)
          (fn [event] (log/error "Error deleting data from IndexedDB: "
                                 (.-message (.-error (.-target event))))))
    (set! (.-onsuccess request)
          (fn [event] (close! channel)))
    channel))

(defn open-database
  "Opens an IndexedDB database.
  The database should have already been created."
  ([name] (open-database name 1))
  ([name version]
   (let [channel (chan)
         request (.open js/window.indexedDB name version)]
     (set! (.-onerror request)
           (fn [event] (log/error "Error opening IndexedDB: "
                                  (.-message (.-error (.-target event))))))
     (set! (.-onsuccess request)
           (fn [event] (go (>! channel (.-result (.-target event))))))
     channel)))

;; The problem is that clj->js is not camelCasing keys

(defn create-database
  "Creates an IndexedDB database and returns a channel that resolves with a database instance.
  If the database has already been created with the specified version,
  just opens the existing one. `object-stores` is a sequence of object
  store maps created via `looped-in.indexeddb/create-object-store`."
  ([name object-stores]
   (create-database name 1 object-stores))
  ([name version object-stores]
   (let [channel (chan)
         request (.open js/window.indexedDB name version)]
     (set! (.-onerror request)
           (fn [event]
             (log/error "Error opening IndexedDB: "
                        (.-message (.-error (.-target event))))))
     (set! (.-onupgradeneeded request)
           (fn [event]
             (let [db (.-result (.-target event))]
               (doseq [object-store object-stores]
                 (let [store (.createObjectStore db
                                                 (:name object-store)
                                                 (clj->js
                                                  {:keyPath
                                                   (:key-path object-store)
                                                   :autoIncrement
                                                   (:auto-increment? object-store)}))]
                   (doseq [index (:indices object-store)]
                     (.createIndex store
                                   (:name index)
                                   (:key-path index)
                                   (clj->js (:opts index)))))))))
     (set! (.-onsuccess request)
           (fn [event]
             (go (>! channel (.-result (.-target event))))))
     channel)))

(defn create-object-store
  "Creates an object store data structure for use with `looped-in.indexeddb/create-database`.
  `indices` is a sequence of index maps
  created via `looped-in.indexeddb/create-index`."
  ([store-name key-path]
   (create-object-store store-name key-path []))
  ([store-name key-path indices]
   (create-object-store store-name key-path indices false))
  ([store-name key-path indices auto-increment?]
   {:name store-name :key-path key-path :indices indices :auto-increment? auto-increment?}))

(defn create-index
  "Creates an index for use with `looped-in.indexeddb/create-object-store`.
  `opts` is a map of options. Valid keys are :unique and :multiEntry, as
  documented at https://developer.mozilla.org/en-US/docs/Web/API/IDBObjectStore/createIndex."
  ([name]
   (create-index name name))
  ([name key-path]
   (create-index name key-path {}))
  ([name key-path opts]
   {:name name :key-path key-path :opts opts}))
