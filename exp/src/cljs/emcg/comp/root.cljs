(ns emcg.comp.root
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [emcg.util :refer [count-map-lists]]
   [emcg.state :refer [count-stims]]
   [emcg.comp.expone :refer [exp-comp]]
   ))

(defn fetching-stims? [app]
  (< (count-stims (:exp-def app))
     (count (apply concat (vals (:stim-infos app))))
     ))

(defn app-to-exp-comp-props [app]
  (merge (select-keys app [:exp-def :stim-infos])
                          {:num-res (count-map-lists (:exp-res app))}))

(defn root-component [app owner]
  (reify
      om/IRender
    (render [_]
      (cond
        (:init-err app)
        (dom/h1 nil "Initialization error")
        (not (:exp-def app))
        (dom/h1 nil "Initializing experiment defn")
        (fetching-stims? app)
        (dom/h1 nil "fetching stims")
        :else
        (om/build* exp-comp (app-to-exp-comp-props app))
      ))))
