(ns emcg.state.data-munge
  (:require
   [emcg.util :refer [count-map-lists]]
  ))

(defn count-stims
  "total number of stimuli, both emo and mcg, for a given experiment"
  [exp-def]
  (let [mcg-blocks (or (:defn exp-def) exp-def)]
    (+ (count mcg-blocks)
       (reduce + (map (comp count :mcg-ids) mcg-blocks)))))

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

;;;; functions on _dereferenced_ app-state

(defn fetching-stims? [app-state]
  (< (count-stims (:exp-def app-state))
     (count (apply concat (vals (:stim-infos app-state))))
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
