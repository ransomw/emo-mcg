(ns emcg.demo.state
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [cljs.core.async :refer [chan <!]]
   ))

(defonce app-state
  (atom {:text "Hello Chestnut.."}))


(defonce update-chan (chan))

(defn listen-for-updates []
  (go
    (while true
      (let [update-info (<! update-chan)
            msg-res (:msg update-info)
            vid-res (:vid update-info)
            img-res (:img update-info)
            thingones-res (:thingones update-info)]
        (if msg-res
          (swap! app-state assoc :text msg-res))
        (if vid-res
          (swap! app-state assoc :vid-url vid-res))
        (if img-res
          (swap! app-state assoc :img-url img-res))
        (if thingones-res
          (swap! app-state assoc :thingone-names thingones-res))
        ))))

(listen-for-updates)
