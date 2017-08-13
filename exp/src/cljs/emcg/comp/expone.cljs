(ns emcg.comp.expone
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [emcg.comp.expone-emo :refer [emo-comp]]
   [emcg.comp.expone-mcg :refer [mcg-comp]]
   [emcg.comp.props :as props]
   ))

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
       (if (props/emo-stim? props)
         (dom/div nil (om/build emo-comp (props/get-emo-props props)))
         (let [{:keys [mcg-id av-idxs]} (props/get-some-mcg-props props)]
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
