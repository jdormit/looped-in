(ns looped-in.analytics
  (:require [goog.dom :as dom]
            [cljs.core.async :refer [go chan <! >!]]
            [looped-in.promises :refer [promise->channel]]
            [looped-in.logging :as log]))

(goog-define amplitude-api-key "FAKE_API_KEY")

(def amplitude-init-code
  (str "(function(e,t){var n=e.amplitude||{_q:[],_iq:{}};var r=t.createElement('script');r.type='text/javascript';r.async=true;r.src='https://cdn.amplitude.com/libs/amplitude-4.1.0-min.gz.js';r.onload=function(){if(e.amplitude.runQueuedFunctions){e.amplitude.runQueuedFunctions()}else{console.log('[Amplitude] Error: could not load SDK')}};var i=t.getElementsByTagName('script')[0];i.parentNode.insertBefore(r,i);function s(e,t){e.prototype[t]=function(){this._q.push([t].concat(Array.prototype.slice.call(arguments,0)));return this}}var o=function(){this._q=[];return this};var a=['add','append','clearAll','prepend','set','setOnce','unset'];for(var u=0;u<a.length;u++){s(o,a[u])}n.Identify=o;var c=function(){this._q=[];return this};var l=['setProductId','setQuantity','setPrice','setRevenueType','setEventProperties'];for(var p=0;p<l.length;p++){s(c,l[p])}n.Revenue=c;var d=['init','logEvent','logRevenue','setUserId','setUserProperties','setOptOut','setVersionName','setDomain','setDeviceId','setGlobalUserProperties','identify','clearUserProperties','setGroup','logRevenueV2','regenerateDeviceId','logEventWithTimestamp','logEventWithGroups','setSessionId'];function v(e){function t(t){e[t]=function(){  e._q.push([t].concat(Array.prototype.slice.call(arguments,0)))}}for(var n=0;n<d.length;n++){t(d[n])}}v(n);n.getInstance=function(e){  e=(!e||e.length===0?'$default_instance':e).toLowerCase();if(!n._iq.hasOwnProperty(e)){n._iq[e]={_q:[]};v(n._iq[e])}return n._iq[e]}  ;e.amplitude=n})(window,document);amplitude.getInstance().init('" amplitude-api-key "');"))

(defn do-not-track
  "Returns true if Do Not Track is enabled"
  []
  (= (.-doNotTrack js/navigator) "1"))

(defn init-amplitude
  "Injects the Amplitude bootstrapping script if DNT is disabled"
  []
  (when-not (do-not-track)
    (dom/appendChild (.-body (dom/getDocument))
                     (dom/createDom "script" nil amplitude-init-code))
    (go (let [user-id (-> js/browser
                          (.-runtime)
                          (.sendMessage (clj->js {:type "getUserId"}))
                          (promise->channel)
                          (<!))]
          (.setUserId (.getInstance js/amplitude) user-id)))))

(defn log-event
  "Logs an event to Amplitude. Returns a channel that resolves with the response from Amplitude"
  ([event-name properties]
   (let [res-channel (chan)]
     (when (and (not (do-not-track)) (exists? js/amplitude))
       (.logEvent (.getInstance js/amplitude)
                  event-name
                  (clj->js properties)
                  (fn [response-code response-body]
                    (go (>! res-channel {:code response-code
                                         :body response-body})))))
     res-channel))
  ([event-name] (log-event event-name nil)))
