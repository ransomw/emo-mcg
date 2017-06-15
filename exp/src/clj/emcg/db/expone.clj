(ns emcg.db.expone

  (:require
   [clojure.string :as s]
   [clojure.set :as set]

   [hugsql.core :as hugsql]
   [honeysql.core :as sql]
   [honeysql.helpers :refer :all]

   [emcg.expone :refer [exp-stim-config]]
   [emcg.rand :refer [rand-multilist]]
   ))

(hugsql/def-db-fns "emcg/db/expone.sql")
(hugsql/def-sqlvec-fns "emcg/db/expone.sql")

;;;;;;;;;;;;;;; helper functions ;;;;

;;;; experiment-specific randomization ;;

;; todo: randomize on same or different stim
(defn rand-idxs-av [num-mcg-stim]
  (map (fn [idx-av]
         {:idx-a (first idx-av)
          :idx-v (last idx-av)})
       (map list
            (rand-multilist num-mcg-stim (range num-mcg-stim))
            (rand-multilist num-mcg-stim (range num-mcg-stim))
            )))

;;;; db ;;

;; init exp ;

(defn add-exp [db-spec]
  (:id (first ;; exactly one row is inserted
        (add-exp-hug db-spec))))

(defn add-emo-stim [db-spec exp-id idx-stim seq-num]
  (:id (first ;; exactly one row is inserted
        (add-emo-stim-hug
         db-spec
         {:exp-id exp-id
          :idx-stim idx-stim
          :seq-num seq-num
          }))))

(defn add-emo-stims [db-spec exp-id num-emo-stim]
  (map
   (fn [[idx-stim seq-num]]
     (add-emo-stim db-spec exp-id idx-stim seq-num))
   (let [idx-stim-list
         (rand-multilist
          num-emo-stim
          (range (:num-emo exp-stim-config)))]
     (map list idx-stim-list (range (count idx-stim-list))))
   ))

(defn add-mcg-stim [db-spec emo-stim-id seq-num idx-a idx-v]
  (:id (first ;; exactly one row is inserted
        (add-mcg-stim-hug
         db-spec
         {:emo-stim-id emo-stim-id
          :seq-num seq-num
          :idx-a idx-a
          :idx-v idx-v
          }))))

;; create the data for a McGuirk stimulus block,
;; a list of maps with keys
;; :seq-num
;; :idx-a
;; :idx-v
(defn gen-mcg-stim-block []
  (let [idxs-av (rand-idxs-av (:num-mcg exp-stim-config))]
    (map (fn [seq-num idx-av]
           (merge {:seq-num seq-num} idx-av))
         (range (count idxs-av)) idxs-av)))

;; -- this could be cleaner :/
;; makes a list of lists, each list containing
;; sequence number, audio index, visual index
;; in that order
(defn gen-mcg-trial-defs []
   (map (fn [mcg-trial-def-map]
          (map (partial get mcg-trial-def-map)
               '(:seq-num :idx-a :idx-v)))
        (gen-mcg-stim-block)))

(defn add-mcg-stim-block [db-spec emo-stim-id]
  (doall
   (map
    (fn [mcg-trial-def-list]
      (apply (partial add-mcg-stim db-spec emo-stim-id)
             mcg-trial-def-list))
    (gen-mcg-trial-defs))))

;; read exp ;

;; an experiment consists of an emotional stimulus,
;; followed by a number of "blocks" of McGuirk-effect
;; stimulus&response trials, each of which contains multiple
;; McGuirk-effect A-V pairings

;; helper that's like clojure.core/partial, except
;; it partially applies the function to its args in reverse order
(defn h-partial-reverse [f & partial-args]
  (comp
   (partial
    apply
    (apply partial
           (cons (comp (partial apply f) reverse list)
                 partial-args)))
   reverse list))

;; helper to transform linear data into hierarchical data
(defn h-rows-to-tree [some-rows id-key]
  (let [all-keyed-ids
        (seq (set (map
                   (fn [res-row] (get res-row id-key))
                   some-rows)))]
    (map
     (fn [id-and-rows]
       {id-key (first id-and-rows)
        :rows-data
        (map (fn [some-row] (dissoc some-row id-key))
             (last id-and-rows))})
     (map list
          all-keyed-ids
          (map (fn [a-keyed-id]
                 (filter (fn [res-row]
                           (= (get res-row id-key) a-keyed-id))
                         some-rows)) all-keyed-ids))
     )))

(defn get-exp [db-spec exp-id]
  (let [rows-res (get-exp-hug
                  db-spec
                  {:exp-id exp-id})
        ]
    (map (fn [tree-node]
           (let [{emo-id :emo_id
                  rows-data :rows-data
                  [{emo-idx :idx_stim
                    seq-num-emo :seq_num_emo}] :rows-data
                  } tree-node]

             {:emo-id emo-id
              :emo-idx emo-idx
              :seq-num seq-num-emo
              :mcg-trials
              (map
               (h-partial-reverse set/rename-keys
                                  {:mcg_id :mcg-id
                                   :seq_num_mcg :seq-num
                                   :idx_v_stim :idx-v
                                   :idx_a_stim :idx-a
                                   })
               ;; matches :rows-data destructure in above let
               (map (h-partial-reverse dissoc
                                       :idx_stim
                                       :seq_num_emo)
                    rows-data))
              }))
         (h-rows-to-tree rows-res :emo_id))
  ))
