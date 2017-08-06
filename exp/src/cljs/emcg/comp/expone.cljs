(ns emcg.comp.expone
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [chan <! >! close!]]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]

   [emcg.data-munge :refer [get-mcg-idx-in-block]]
   [emcg.act :as act]
   ))

(defn emo-comp [{:keys [emo-id vid-url]} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (-> owner
          (.-video)
          (.addEventListener
           "ended"
           #(act/add-emo-res emo-id))
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
             :crossorigin "anonymous"
             :ref (fn [el] (set! (.-video owner) el))
             }))
       (dom/button
        #js {:onClick #(-> owner (.-video) (.play))}
        "start stim")
       )
      )))

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
             :crossorigin "anonymous"
             ;; :ref "video"
             :ref (fn [el] (set! (.-video owner) el))
             })
       (if play-ended
         (apply
          dom/div nil
          (map
           (fn [av-idx]
             (dom/button
              #js {:onClick #(act/add-mcg-res
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

;;;;;;;;;;;;; exp-comp
;;; duplicates dupe dedupe data-munge/get-app-state-stim-id-head here
(defn emo-stim? [exp-comp-props]
  (let [{:keys [exp-def stim-infos num-res]} exp-comp-props]
    (= (:mcg num-res)
       (reduce
        + (map (comp count :mcg-ids)
               (take (:emo num-res) (:defn exp-def)))))
    ))

(defn get-emo-props [{:keys [exp-def stim-infos num-res]
                           :as exp-comp-props}]
  (let [emo-id (:emo-id (nth (:defn exp-def) (:emo num-res)))]
    {:emo-id emo-id
     :vid-url (:url
               (first (filter #(= emo-id (:id %))
                              (:emo stim-infos))))
     }))

(defn get-some-mcg-props [{:keys [exp-def stim-infos num-res]
                           :as exp-comp-props}]
  (let [mcg-idx-in-block (get-mcg-idx-in-block exp-comp-props)
        emo-idx (- (:emo num-res) 1) ;; from _zero_
        mcg-id (-> (:defn exp-def)
                   (nth emo-idx)
                   (get :mcg-ids)
                   (nth mcg-idx-in-block))
        av-idxs (-> (:defn exp-def)
                    (nth emo-idx)
                    (get :av-idxs)
                    (nth mcg-idx-in-block))]
        {:mcg-id mcg-id :av-idxs av-idxs}))

(defn exp-comp
  [{:keys [exp-def stim-infos num-res] :as props} owner]
  (reify
      om/IRender
    (render [this]
      (dom/div
       nil
       (dom/div
        nil
        (dom/h3 nil "experiment component")
        (dom/span nil (str "exp id: " (:id exp-def)))
        )
       (if (emo-stim? props)
         (dom/div nil (om/build emo-comp (get-emo-props props)))
         (let [{:keys [mcg-id av-idxs]} (get-some-mcg-props props)]
             (om/build
              mcg-comp
              {:mcg-id mcg-id
               :vid-url (-> (->> (:mcg stim-infos)
                                 (filter #(= mcg-id (:id %)))
                                 (first))
                            (get :url))
               :av-idxs av-idxs
               }))
         )
       )
      )))
