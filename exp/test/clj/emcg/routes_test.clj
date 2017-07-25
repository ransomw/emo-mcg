(ns emcg.routes-test
  (:require
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]

   [clojure.test :refer :all]
   [clojure.edn :as edn]
   [clojure.string :as str]
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
  (let [res (test-handler
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

(deftest get-stim-data
  (let [res (test-handler
             (mock/request :post "/exp"))
        body (edn/read-string (:body res))
        {exp-id :id exp-defn :defn} body]
    (->>
     exp-defn
     (map
      (fn [{:keys [emo-id mcg-ids av-idxs]}]
        (let [mcg-endpoints
              (map
               (partial str
                        (str "/exp/" exp-id "/emo/" emo-id "/mcg/"))
               mcg-ids)
              emo-res
              (test-handler
               (mock/request :get (str "/exp/" exp-id "/emo/" emo-id)))
              mcg-resps
              (map #(test-handler (mock/request :get %)) mcg-endpoints)
              check-vid-resp
              (fn [res]
                (is (= "video"
                       (first (str/split
                               (get (:headers res) "Content-Type")
                               #"/")))))
              ]
          (check-vid-resp emo-res)
          (doall (map check-vid-resp mcg-resps))
        )))
     doall)
    ))

(deftest init-exp-db
  (let [res (test-handler
             (mock/request :post "/exp"))
        body (edn/read-string (:body res))
        {exp-id :id exp-defn-http :defn} body
        exp-defn-db (db/get-exp exp-id)]
    (->>
     exp-defn-db
     (map
      (fn [{:keys [emo-id emo-idx seq-num mcg-trials]}]
        (let [{emo-id-http :emo-id
               mcg-ids :mcg-ids
               av-idxs :av-idxs} (nth exp-defn-http seq-num)]
          (is (= emo-id emo-id-http))
        )))
     doall)
    ))
