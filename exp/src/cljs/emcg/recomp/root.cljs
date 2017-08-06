(ns emcg.recomp.root
  (:require
   [emcg.recomp.state :as st]
   )
  )
;;  [reagent.core :as r]

(defn root-component []
  [:div.someclass
   [:p
    [:span {:font-size "2em"} "placeholder component"]]
   [:p "while setting up scaffolding"]
   ])
