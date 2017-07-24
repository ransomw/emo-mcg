(ns emcg.db-test
  (:require
   [clojure.test :refer :all]
   [emcg.db :as db]
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
      (->>
       exp-defn
       (map
        (fn [emo-block]
          (is (contains? (set (range (:num-emo exp-stim-config)))
                         (:emo-idx emo-block)))
          
          ))
       doall)
    )))
