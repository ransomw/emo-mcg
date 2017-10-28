(ns emcg.db.expone
  (:require
   [clojure.string :as s]
   [clojure.set :as set]
   [hugsql.core :as hugsql]
   [emcg.db.util :as util]
   [emcg.db.expone_hug :as hug]
   [emcg.db.expone-logic :refer [gen-mcg-stim-block]]
   [emcg.db.expone-defs :refer [exp-stim-config]]
   ))

(hugsql/def-db-fns "emcg/db/expone_schema.sql")
(hugsql/def-sqlvec-fns "emcg/db/expone_schema.sql")

;;;;;;;;;;;;;;; helper functions ;;;;


;;;; db ;;

;; init exp ;

(defn add-exp [db]
  (:id (first ;; exactly one row is inserted
        (hug/add-exp db))))

(defn add-emo-stims [db exp-id num-emo-stim]
  (map
   (fn [[idx-stim seq-num]]
     (:id (first
           (hug/add-emo-stim db {:exp-id exp-id
                                      :idx-stim idx-stim
                                      :seq-num seq-num
                                      }))))
   (let [idx-stim-list
         (util/rand-multilist
          num-emo-stim
          (range (:num-emo exp-stim-config)))]
     (map list idx-stim-list (range (count idx-stim-list))))
   ))

(defn add-mcg-stim-block [db emo-stim-id]
  (doall
   (map
    (fn [mcg-trial-def-map]
      (:id (first ;; exactly one row is inserted
            (hug/add-mcg-stim
             db
             (merge {:emo-stim-id emo-stim-id}
                    mcg-trial-def-map)))))
    (gen-mcg-stim-block)
    )))

;; read exp ;

;; an experiment consists of an emotional stimulus,
;; followed by a number of "blocks" of McGuirk-effect
;; stimulus&response trials, each of which contains multiple
;; McGuirk-effect A-V pairings

(defn get-exp [db exp-id]
  (let [rows-res (hug/get-exp
                  db
                  {:exp-id exp-id})
        ]
    (map (fn [{emo-id :emo_id
               rows-data :maps-with-val-at-key}]
           {:emo-id emo-id
            :emo-idx (util/seq-only (set (map :idx_stim rows-data)))
            :seq-num (util/seq-only (set (map :seq_num_emo rows-data)))
            :mcg-trials
            (->>
             rows-data
             (map #(dissoc % :idx_stim :seq_num_emo))
             (map #(set/rename-keys % {:mcg_id :mcg-id
                                       :seq_num_mcg :seq-num
                                       :idx_v_stim :idx-v
                                       :idx_a_stim :idx-a
                                       }))
             )
            })
         (util/partition-list-of-maps rows-res :emo_id))
    ))


(defn get-mcg-entry [db mcg-id]
  (let [rows-res (hug/get-mcg
                  db
                  {:mcg-id mcg-id})
        ]
    (if (= 1 (count rows-res))
      (let [row-res (first rows-res)]
        (if (= (-> row-res (get :id)) mcg-id)
          (->
           row-res
           (set/rename-keys {:idx_a_stim :idx-a-stim
                             :idx_v_stim :idx-v-stim
                             :idx_resp :idx-resp})
           (dissoc :expone_emo_id :seq_num :id)
           )
      )))))


(defn set-mcg-res [db mcg-id idx-resp]
  (let [mcg-entry (get-mcg-entry db mcg-id)]
    (if (not (nil? mcg-entry))
      (let [{:keys [idx-a-stim idx-v-stim]
             idx-resp-curr :idx-resp} mcg-entry]
        (if (and (nil? idx-resp-curr)
                 (or (= idx-a-stim idx-resp) (= idx-v-stim idx-resp)))
          (hug/set-mcg-resp
           db
           {:mcg-id mcg-id :idx-resp idx-resp})
          )))))
