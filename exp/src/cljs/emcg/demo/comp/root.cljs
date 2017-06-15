(ns emcg.demo.comp.root
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [emcg.demo.comp.dom-test :refer [dom-test-view]]
   ))

(defn root-component [app owner]
  (reify
    om/IRender
    (render [_]

      (dom/div
       nil
       (om/build
        dom-test-view
        {:text (:text app)
         :vid-url (:vid-url app)
         :img-url (:img-url app)
         :thingone-names (:thingone-names app)
         }))

      ;; (dom/div nil (dom/h3 nil (:text app)))

      )))
