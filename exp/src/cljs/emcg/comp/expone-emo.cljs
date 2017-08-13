(ns emcg.comp.expone-emo
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [emcg.act :refer [add-emo-res]]
   ))

(defn emo-comp [{:keys [emo-id vid-url]} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (-> owner
          (.-video)
          (.addEventListener
           "ended"
           #(add-emo-res emo-id))
      ))
    om/IRender
    (render [this]
      (dom/div
       nil
       (dom/h4 nil "emo comp")
       (dom/span nil (str "emo id: " emo-id))
      (dom/div
       nil
       (dom/video
        #js {:src vid-url
             :controls false
             :crossOrigin "anonymous"
             :ref (fn [el] (set! (.-video owner) el))
             }))
       (dom/button
        #js {:onClick #(-> owner (.-video) (.play))}
        "start stim")
       )
      )))
