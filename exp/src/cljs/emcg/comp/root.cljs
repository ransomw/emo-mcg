(ns emcg.comp.root
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [emcg.comp.expone :refer [exp-comp]]
   [emcg.state.data-munge :as m]
   [emcg.comp.props :as props]
   ))

(defn root-component [app owner]
  (reify
      om/IRender
    (render [_]
      (cond
        (:init-err app)
        (dom/h1 nil "Initialization error")
        (not (:exp-def app))
        (dom/h1 nil "Initializing experiment defn")
        (m/fetching-stims? app)
        (dom/h1 nil "fetching stims")
        :else
        (om/build* exp-comp (props/make-exp-comp-props app))
      ))))
