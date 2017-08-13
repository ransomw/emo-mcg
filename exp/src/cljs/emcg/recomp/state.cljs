(ns emcg.recomp.state
  (:require
   [reagent.core :as r]
   [emcg.state.core :as st]
   ))

(defonce app-state (r/atom @st/app-state))

(defonce cp-listeners
  (let [cb-cp (fn [k r o n] (swap! app-state assoc k (k n)))]
    (doall
     (map #(add-watch st/app-state % cb-cp)
          (keys @st/app-state)))))
