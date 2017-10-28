(ns emcg.db-test
  (:require
   [clojure.test :refer :all]
   [com.stuartsierra.component :as component]

   [emcg.config :refer [config]]
   [emcg.db.component :refer [new-database]]
   [emcg.db.core :as db]
   [emcg.db.expone :as eone]
   [emcg.db.expone-defs :refer [exp-stim-config]]
   ))

(def ^{:dynamic true} *db*)

(defn db-fixture [f]
  (binding [*db* (component/start
                  (new-database (:db-spec (config))))]
    (db/reset-db! *db*)
    (f)
    (component/stop *db*)
    ))

(use-fixtures :each db-fixture)

(def num-emo-stim 2)

(defn get-mcg-entry [mcg-id]
  (eone/get-mcg-entry (:connection *db*) mcg-id))

(deftest init-exp-test
  (let [exp-id (db/init-exp! *db* num-emo-stim)]
    (is (integer? exp-id))
    (let [exp-defn (db/get-exp *db* exp-id)]
      (is (seq? exp-defn))
      (is (= num-emo-stim (count exp-defn)))
      (is (= (set (range (count exp-defn)))
             (set (map :seq-num exp-defn))))
      (is (= (count exp-defn)
             (count (set (map :emo-id exp-defn)))))
      (let [mcg-ids
            (map :mcg-id (flatten (map :mcg-trials exp-defn)))]
        (is (= (count mcg-ids) (count (set mcg-ids)))))
      (->>
       exp-defn
       (map
        (fn [{:keys [emo-id emo-idx seq-num mcg-trials]}]
          (is (integer? emo-id))
          (is (integer? emo-idx))
          (is (integer? seq-num))
          (is (seq? mcg-trials))
          (is (contains?
               (set (range (:num-emo exp-stim-config)))
               emo-idx))
          (is (= (set (range (count mcg-trials)))
                 (set (map :seq-num mcg-trials))))
          (->>
           mcg-trials
           (map
            (fn [{:keys [mcg-id seq-num idx-v idx-a]}]
              (is (integer? mcg-id))
              (is (integer? seq-num))
              (is (contains?
                   (set (range (:num-mcg exp-stim-config)))
                   idx-v))
              (is (contains?
                   (set (range (:num-mcg exp-stim-config)))
                   idx-a))
              ))
           doall)
          ))
       doall)
    )))

(deftest mcg-res-test
  (let [exp-id (db/init-exp! *db* num-emo-stim)]
    (let [{:keys [mcg-id idx-v]} (-> (db/get-exp *db* 1)
                                     (first)
                                     (get :mcg-trials)
                                     (first))]
      (let [mcg-entry (get-mcg-entry mcg-id)]
        (is (map? mcg-entry))
        (is (= (set (keys mcg-entry))
               (set [:idx-a-stim :idx-v-stim :idx-resp])))
        (is (nil? (:idx-resp mcg-entry)))
        )
      (is (integer? (db/set-mcg-res! *db* mcg-id idx-v)))
      (let [mcg-entry (get-mcg-entry mcg-id)]
        (is (= idx-v (:idx-resp mcg-entry)))
        )
      )))

(deftest reset-db!-test
  (let [exp-id (db/init-exp! *db* num-emo-stim)
        {:keys [mcg-id idx-v]} (-> (db/get-exp *db* 1)
                                   (first)
                                   (get :mcg-trials)
                                   (first))]
    (is (not (= 0 (count (db/get-exp *db* exp-id)))))
    (is (not (nil? (get-mcg-entry mcg-id))))
    (db/reset-db! *db*)
    (is (= 0 (count (db/get-exp *db* exp-id))))
    (is (nil? (get-mcg-entry mcg-id)))
    ))
