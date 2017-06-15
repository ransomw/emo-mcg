(ns emcg.demo.core
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [cljs.core.async :refer [<! close!]]
   [om.core :as om :include-macros true]

   [emcg.demo.comp.root :as comp-root]
   [emcg.demo.state :as st]
   [emcg.demo.act :as act]
   ))

(defn init-demo []
  (act/fetch-text)
  (act/fetch-vid)
  (act/fetch-img)
  (act/fetch-thingones)
  )

(defn demo-main []
  (om/root
   comp-root/root-component
   st/app-state
   {:target (js/document.getElementById "app")}))

(defn run-demo []
  (init-demo)
  (demo-main))
