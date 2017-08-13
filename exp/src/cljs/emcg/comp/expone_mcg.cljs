(ns emcg.comp.expone-mcg
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [emcg.act :refer [add-mcg-res]]
   ))

(defn mcg-comp [{:keys [mcg-id vid-url av-idxs]} owner]
  (reify
      om/IInitState
    (init-state [_]
      {:play-ended false})
    om/IDidMount
    (did-mount [_]
      (let [video-el (-> owner (.-video))]
        (-> video-el
            (.addEventListener
             "ended"
             #(om/set-state! owner :play-ended true))
            )
        (-> video-el (.play))
        ))
    om/IRenderState
    (render-state [this {:keys [play-ended]}]
      (dom/div
       nil
       (dom/video
        #js {:src vid-url
             :controls false
             :crossOrigin "anonymous"
             ;; :ref "video"
             :ref (fn [el] (set! (.-video owner) el))
             })
       (if play-ended
         (apply
          dom/div nil
          (map
           (fn [av-idx]
             (dom/button
              #js {:onClick #(add-mcg-res
                              mcg-id
                              {:av-idx av-idx})}
              (str av-idx)))
           ;; todo:
           ;; * play new video with each click
           ;; * hide buttons when play in progress (here and emo stim)
           ;; * don't create stim w/ matched AV idx (backend)
           av-idxs)
          ))
       )
      )))
