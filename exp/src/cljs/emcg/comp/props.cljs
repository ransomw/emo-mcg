(ns emcg.comp.props
  (:require
   [emcg.util :refer [count-map-lists]]
   [emcg.state.data-munge :refer [get-mcg-idx-in-block]]
   ))

(defn make-exp-comp-props [app-state]
  (merge (select-keys app-state [:exp-def :stim-infos])
         {:num-res (count-map-lists (:exp-res app-state))}))

;;; compare with data-munge/get-app-state-stim-id-head
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
