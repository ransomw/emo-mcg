(ns emcg.comp.root
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [emcg.state :refer [count-stims]]
   [emcg.comp.expone :refer [exp-comp]]
   ))

(defn root-component [app owner]
  (reify
      om/IRender
    (render [_]
      (cond
        (:init-err
         app) (dom/h1 nil "Initialization error")
        (not
         (:exp-def
          app)) (dom/h1 nil "Initializing experiment defn")
        (< (count-stims (:exp-def app))
           (count
            (apply
             concat
             (vals
              (:stim-infos
               app))))) (dom/h1 nil "fetching stims")
        :else
        (om/build* exp-comp
                   (merge (select-keys app [:exp-def :stim-infos])
                          {:num-res
                           ;; todo: generalize and move this "map-vals"
                           ;; functionality to a common/ util module
                           (reduce
                            (fn [acc [key val]]
                              (assoc acc key (count val))) {}
                            (:exp-res app))}))
      ))))
