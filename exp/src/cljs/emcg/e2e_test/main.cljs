(ns emcg.e2e-test.main
  (:require
   [reagent.core :as r]
   [ajax.core :refer [GET]]
   [emcg.config :refer [base-url]]
   ))

(def state (r/atom :loading))

(defn main-view []
  (case @state
    :loading [:div#loading "Loading..."]
    :started [:div#done "Done"]))

(defn render-app [target]
  (r/render [main-view] target)
  (GET
   (str base-url "/demo/hello")
   {:handler (fn [data] (reset! state :started))}))
