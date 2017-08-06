(ns emcg.db-test
  (:require
   [clojure.test :refer :all]
   [emcg.db :as db]
   [emcg.db.expone :as eone]
   [emcg.expone :refer [exp-stim-config]]
   ))

(defn db-fixture [f]
  (db/reset-db!)
  (f)
  ; no teardown
  )

(use-fixtures :each db-fixture)

(deftest init-exp-test
  (let [num-emo-stim 2
        exp-id (db/init-exp! num-emo-stim)]
    (is (integer? exp-id))
    (let [exp-defn (db/get-exp exp-id)]
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
  (let [num-emo-stim 2
        exp-id (db/init-exp! num-emo-stim)]
    (let [{:keys [mcg-id idx-v]} (-> (db/get-exp 1)
                                     (first)
                                     (get :mcg-trials)
                                     (first))]
      (let [mcg-entry (eone/get-mcg-entry db/db-spec mcg-id)]
        (is (map? mcg-entry))
        (is (= (set (keys mcg-entry))
               (set [:idx-a-stim :idx-v-stim :idx-resp])))
        (is (nil? (:idx-resp mcg-entry)))
        )
      (is (integer? (db/set-mcg-res! mcg-id idx-v)))
      (let [mcg-entry (eone/get-mcg-entry db/db-spec mcg-id)]
        (is (= idx-v (:idx-resp mcg-entry)))
        )
      )))
