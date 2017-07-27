(ns emcg.e2e-test
  (:require
   [clojure.test :refer :all]
   [doo.core :as doo]
   [cljs.build.api :as cljs]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.cors :refer [wrap-cors]]
   [emcg.db :as db]
   [emcg.server :refer [http-handler]]
   ))

(def http-handler-cors
  (-> http-handler
      (wrap-cors
       :access-control-allow-origin [#".*"]
       :access-control-allow-methods [:get :put :post :delete])
      ))

;; server start/stop lifted from dnolen's SO post
(defonce server
  ;; ??? what's this `#'` syntax?
  (run-jetty #'http-handler-cors {:port 3333 :join? false}))

;; overall test strategy is lifted from
;; https://github.com/bensu/doo/wiki/End-to-end-testing-example
(use-fixtures :each
  (fn [f]
    (db/reset-db!)
    ;; 1. Start the backend system at port 3333
    (.start server)
    ;; ??? is this
    ;; (assoc :nrepl {}) ;; Mock unnecessary dependencies before start
    (f) ;; 2. run the test, i,e. end-to-end-suite
    (.stop server))) ;; 3. Stop the backend system

(deftest end-to-end-suite
  (let [compiler-opts {:main 'emcg.e2e-runner
                       :output-to "out/e2e_test.js"
                       :output-dir "out"
                       :asset-path "out"
                       :optimizations :none}]
    ;; Compile the ClojureScript tests
    (cljs/build (apply cljs/inputs ["src/cljs"
                                    "src/cljc"
                                    "test/cljs"
                                    "env/test/cljs"
                                    ])
                compiler-opts)
    ;; Run the ClojureScript tests and check the result
    (is (zero? (:exit (doo/run-script :phantom compiler-opts {}))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (use-fixtures :each
;;   (fn [f]
;;     ;; 1. Start the backend system at port 3333
;;     (let [system (-> (system/new-system {:http {:port 3333}
;;                                          :app {:middleware [(fn [handler]
;;                                                               (wrap-cors handler
;;                                                                          :access-control-allow-origin [#".*"]
;;                                                                          :access-control-allow-methods [:get :put :post :delete]))]}})
;;                      (assoc :nrepl {}) ;; Mock unnecessary dependencies before start
;;                      component/start)]
;;       ;; Perform post start setup here, like adding a test user to a database
;;       (f) ;; 2. run the test, i,e. end-to-end-suite
;;       (component/stop system)))) ;; 3. Stop the backend system
