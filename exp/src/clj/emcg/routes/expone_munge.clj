(ns emcg.routes.expone-munge
  (:require
   [clojure.set :refer [rename-keys]]
   [emcg.hroutes :refer [route-print]]
   [emcg.expone :refer [emo-stim-filenames mcg-stim-filenames]]
   ))

;;;; various data-munging -- glue between db api and http api

(defn shape-expone-emcg-block [emcg-block]
  (let [mcg-data-list-sorted
        (->>
         (:mcg-trials emcg-block)
         (sort-by :seq-num)
         (map #(dissoc % :seq-num)))]
    (->
     emcg-block
     (dissoc :mcg-trials)
     (merge
      {:mcg-ids (map :mcg-id mcg-data-list-sorted)}
      ;; randomize index ordering before sending over the wire,
      ;; so it's not clear which is the A or V index
      {:av-idxs (map (comp shuffle list)
                     (map :idx-a mcg-data-list-sorted)
                     (map :idx-v mcg-data-list-sorted))})
     )))

(defn shape-expone-data [exp-defn-db]
  (->>
   exp-defn-db
   (map shape-expone-emcg-block)
   (sort-by :seq-num)
   (map #(dissoc % :seq-num :emo-idx))
   ))

(defn get-emcg-block [exp-defn-db emo-id]
  (let [emo-datas
        (filter #(= (:emo-id %) emo-id) exp-defn-db)]
    (if (= 1 (count emo-datas))
      (first emo-datas))))

(defn get-emo-filename [exp-defn-db emo-id]
  (let [emcg-block (get-emcg-block exp-defn-db emo-id)]
    (if emcg-block
      (nth emo-stim-filenames (:emo-idx emcg-block)))
    ))


(defn get-mcg-filename [exp-defn-db emo-id mcg-id]
  (let [emcg-block (get-emcg-block exp-defn-db emo-id)]
    (if emcg-block
      (let [mcg-datas (filter #(= (:mcg-id %) mcg-id)
                              (:mcg-trials emcg-block))]
        (if (= 1 (count mcg-datas))
          (let [{idx-a :idx-a idx-v :idx-v} (first mcg-datas)]
            (-> mcg-stim-filenames
                (nth idx-v)
                (nth idx-a))
            ))))))
