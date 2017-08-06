(ns emcg.data-munge
  (:require
   [emcg.util :refer [count-map-lists]]
  ))

(defn get-mcg-idx-in-block
  [{{exp-defn :defn} :exp-def
    {num-res-emo :emo num-res-mcg :mcg} :num-res}]
  (- num-res-mcg
     (reduce + (->> exp-defn
                   (take (- num-res-emo 1))
                   (map :mcg-ids)
                   (map count)
                   ))
     ))

(defn get-app-state-stim-id-head [app-state]
  (let [{{exp-defn :defn
          exp-id :id} :exp-def
         exp-res :exp-res
         } app-state
        num-res (count-map-lists exp-res)
        emo-idx (- (:emo num-res) 1) ;; from _zero_
        ]
    {:emo-id
     (-> exp-defn
         (nth emo-idx)
         (get :emo-id))
     :mcg-id
     (-> exp-defn
         (nth emo-idx)
         (get :mcg-ids)
         (nth (get-mcg-idx-in-block
               {:exp-def {:defn exp-defn} :num-res num-res}))
         )}
    ))
