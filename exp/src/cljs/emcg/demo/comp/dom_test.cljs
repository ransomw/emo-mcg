(ns emcg.demo.comp.dom-test
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [emcg.demo.act :as act]
   ))

(defn on-button-click-1 []
  (act/create-thingone "nameless, nameless"))

(defn on-button-click-2 []
  (act/create-thingone "souless, saneless"))


(defn dom-test-view [{:keys [text vid-url img-url thingone-names]}
                     owner]
  (reify
      om/IRender
    (render [_]
      (dom/div
       nil
       ;; test text
       (dom/h2 nil text)
       ;; test db records
       (dom/div
        nil
        (dom/button
         #js {:onClick on-button-click-1
              :style #js {:marginBottom "2em"}
              }
         "Heyhey!")
        (dom/button
         #js {:onClick on-button-click-2
              :style #js {:marginBottom "2em"}
              }
         "Say, say..")
       (apply
        dom/ul
        #js {:style #js {:listStyle "none"}}
        (map (fn [li-text]
               (dom/li nil li-text))
             thingone-names)
        ))
       ;; test video
       (dom/div
        (clj->js {:style {:marginBottom "1.5em"}})
        (if vid-url
          (dom/video
           #js {:src vid-url
                :controls true
                :crossorigin "anonymous"})
          (dom/span nil "video url not set")))
       ;; test image
       (dom/div
        (clj->js {:style {:marginBottom "3em"}})
        (if img-url
          (dom/img
           #js {:src img-url})
          (dom/span nil "image url not set")))
       ))))
