(ns emcg.core
  (:require
   [om.core :as om :include-macros true]
   [emcg.state.core :as st]
   [emcg.act :as act]
   [emcg.comp.root :as comp-root]
   [emcg.demo.core :refer [run-demo]]
   ))

(enable-console-print!)

(defn init-emcg []
  (let [exp-def (:exp-def (deref st/app-state))]
    (if (not exp-def)
      (act/create-exp)
      (do (print "client state already initialized"
                 "with experiement definition."
                 "fetching stimulus files.")
          (act/fetch-stims exp-def)))))

(defn emcg-main []
  (om/root
   comp-root/root-component
   st/app-state
   {:target (js/document.getElementById "app")}))

(defn run-emcg []
  (init-emcg)
  (emcg-main))

(run-emcg)

;; (run-demo)

