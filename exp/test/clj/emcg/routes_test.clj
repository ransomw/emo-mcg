(ns emcg.routes-test
  (:require
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]

   [clojure.test :refer :all]
   [clojure.edn :as edn]
   [ring.mock.request :as mock]

   [emcg.server :as s]
   [emcg.routes :as r]
   [emcg.db :as db]
   [emcg.expone :refer [exp-stim-config]]
   ))

(def test-handler
  (->
   (r/routes-expone)
   (wrap-defaults api-defaults)
   (wrap-restful-format :format [:edn])
   ))

(defn routes-fixture [f]
  (db/reset-db!)
  (f)
  ; no teardown
  )

(use-fixtures :each routes-fixture)

(deftest init-exp
  (let [res
        (test-handler
         (mock/request :post "/exp"))
        body (edn/read-string (:body res))]
    (is (not (nil? res)))
    (is (map? res))
    (is (= 200 (:status res)))
    (is (map body))
    (let [{exp-id :id exp-defn :defn} body]
      (is (integer? exp-id))
      (is (list? exp-defn))
      (->>
       exp-defn
       (map
        (fn [{:keys [emo-id mcg-ids av-idxs]}]
          (is (integer? emo-id))
          (is (= (set '(true)) (set (map integer? mcg-ids))))
          (is (= (set '(true)) (set (map integer? (flatten av-idxs)))))
          (is (= (set '(2)) (set (map count av-idxs))))
          (is (= (count mcg-ids) (count (set mcg-ids))))
          (is (= (set '(true))
                 (set (->>
                       (flatten av-idxs)
                       (map
                        #(contains?
                          (set (range (:num-mcg exp-stim-config))) %))
                       doall))))
          ))
       doall)
      (is (= (count exp-defn) (count (set (map :emo-id exp-defn)))))
      (is (= 1 (count (set (map count (map :mcg-ids body))))))
      (is (= 1 (count (set (map count (map :av-idxs body))))))
      )))
