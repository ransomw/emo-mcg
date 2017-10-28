(ns emcg.routes-test
  (:require
   [clojure.test :refer :all]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [com.stuartsierra.component :as component]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [ring.mock.request :as mock]

   [emcg.routes.expone :as r]
   [emcg.routes.expone-munge :refer [shape-expone-data]]
   [emcg.config :refer [config]]
   [emcg.db.component :refer [new-database]]
   [emcg.db.core :as db]
   [emcg.db.expone :as eone]
   [emcg.db.expone-defs :refer [exp-stim-config]]
   ))

(def ^{:dynamic true} *db*)
(def ^{:dynamic true} *test-handler*)

(defn make-test-handler []
  (->
   (r/routes-expone {:db *db*})
   (wrap-defaults api-defaults)
   (wrap-restful-format :format [:edn])
   ))

(defn routes-fixture [f]
  (binding [*db* (component/start
                  (new-database (:db-spec (config))))]
    (binding [*test-handler* (make-test-handler)]
      (db/reset-db! *db*)
      (f))
    (component/stop *db*)
    ))

(use-fixtures :each routes-fixture)

(defn get-mcg-entry [mcg-id]
  (eone/get-mcg-entry (:connection *db*) mcg-id))

(defn check-exp-defn [exp-defn]
  (is (seq? exp-defn))
  (doall
   (map
    (fn [emo-block-info]
      (is (= (set '(:emo-id :mcg-ids :av-idxs))
             (set (keys emo-block-info)))))
    exp-defn
    ))
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
  (is (= (count exp-defn) (count (set (map :emo-id exp-defn))))))

(deftest init-exp-test
  (let [res (*test-handler*
             (mock/request :post "/exp"))
        body (edn/read-string (:body res))]
    (is (not (nil? res)))
    (is (map? res))
    (is (= 200 (:status res)))
    (is (map body))
    (let [{exp-id :id exp-defn :defn} body]
      (is (integer? exp-id))
      (is (list? exp-defn))
      (check-exp-defn exp-defn)
      (is (= 1 (count (set (map count (map :mcg-ids body))))))
      (is (= 1 (count (set (map count (map :av-idxs body))))))
      )))

(deftest get-stim-data-test
  (let [res (*test-handler*
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
              (*test-handler*
               (mock/request :get (str "/exp/" exp-id "/emo/" emo-id)))
              mcg-resps
              (map #(*test-handler* (mock/request :get %)) mcg-endpoints)
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

(deftest init-exp-over-the-wire-test
  (let [res (*test-handler*
             (mock/request :post "/exp"))
        body (edn/read-string (:body res))
        {exp-id :id exp-defn-http :defn} body
        exp-defn-db (db/get-exp *db* exp-id)]
    (= (shape-expone-data exp-defn-db)
       exp-defn-http)
    ))

(deftest shape-expone-data-test
  (let [exp-id (db/init-exp! *db* r/*num-emo-stims*)
        exp-defn-db (db/get-exp *db* exp-id)
        exp-defn-reshaped (shape-expone-data exp-defn-db)]
    (check-exp-defn exp-defn-reshaped)
    (->>
     exp-defn-db
     (map
      (fn [{:keys [emo-id emo-idx seq-num mcg-trials]}]
        (let [{emo-id-http :emo-id
               mcg-ids :mcg-ids
               av-idxs :av-idxs} (nth exp-defn-reshaped seq-num)]
          (is (= emo-id emo-id-http))
        )))
     doall)
    ))

(deftest mcg-resp-test
  (let [res-init-exp (*test-handler*
                      (mock/request :post "/exp"))
        body (edn/read-string (:body res-init-exp))
        {exp-id :id exp-defn :defn} body
        emo-id (-> exp-defn (first) (:emo-id))
        mcg-id (-> exp-defn (first) (:mcg-ids) (first))
        [av-idx1 av-idx2] (-> exp-defn (first) (:av-idxs) (first))
        post-resp-url (str/join ["/exp/" exp-id "/emo/" emo-id
                                 "/mcg/" mcg-id "/resp"])
        mcg-entry-before (get-mcg-entry mcg-id)
        res (*test-handler*
             (-> (mock/request :post post-resp-url)
                 (mock/header :content-type "application/edn")
                 ;; without casting to a string, ring-mock
                 ;; will set content-type to form data
                 (mock/body (str {:idx-resp av-idx1}))
                 )
             )
        mcg-entry-after (get-mcg-entry mcg-id)
        ]
    (is (integer? exp-id))
    (is (integer? emo-id))
    (is (integer? mcg-id))
    (is (integer? av-idx1))
    (is (not (nil? res)))
    (is (= 200 (:status res)))
    (is (nil? (:idx-resp mcg-entry-before)))
    (is (= av-idx1 (:idx-resp mcg-entry-after)))
    ))
