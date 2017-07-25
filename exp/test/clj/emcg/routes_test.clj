(ns emcg.routes-test
  (:require
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]

   [clojure.test :refer :all]
   [ring.mock.request :as mock]

   [emcg.server :as s]
   [emcg.routes :as r]
   [emcg.db :as db]
   [emcg.expone :refer [exp-stim-config]]
   ))

(def test-handler
  (->
   ;; (r/routes-expone)
   r/routes-main
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
        (
         ;; test-handler
         s/http-handler
         (mock/request :get
                       "/"
                       ))]
    (is (not (nil? res)))
    ))
