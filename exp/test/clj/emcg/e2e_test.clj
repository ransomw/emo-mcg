(ns emcg.e2e-test
  (:require
   [clojure.string :as s]
   [clojure.test :refer :all]
   [doo.core :as doo]
   [cljs.build.api :as cljs]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.cors :refer [wrap-cors]]
   [emcg.db-test :refer [get-mcg-entry]]
   [emcg.db.core :as db]
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

(defn run-doo-phantom [cljs-runner-main]
  (let [compiler-opts {:main cljs-runner-main
                       :output-to "out/e2e_test.js"
                       :output-dir "out"
                       :asset-path "out"
                       :optimizations :none}
        phantom-path (s/join " "
                             [
                              "phantomjs"
                              ;; _supposedly_ enables CORS
                              "--web-security=false"
                              ;; "--debug=true"
                              "--local-to-remote-url-access=true"
                              ])]
    ;; Compile the ClojureScript tests
    (cljs/build (apply cljs/inputs ["src/cljs"
                                    "src/cljc"
                                    "test/cljs"
                                    "env/test/cljs"
                                    ])
                compiler-opts)
    ;; Run the ClojureScript tests and check the result
    (->> (doo/run-script
                     :phantom
                     compiler-opts
                     {:paths {:phantom phantom-path} :debug true})
                    (:exit))
    ))

(deftest e2e-suite-no-db
  (is (zero? (run-doo-phantom 'emcg.e2e-runner))))

(deftest e2e-suite-add-mcg-res-no-db
  (is (zero? (run-doo-phantom 'emcg.e2e-runner-add-mcg-res-no-db))))

(deftest e2e-suite-create-exp
  (is (zero? (run-doo-phantom 'emcg.e2e-runner-create-exp)))
  (is (not (= 0 (count (db/get-exp 1)))))
  (is (= 0 (count (db/get-exp 0))))
  )

(deftest e2e-suite-add-mcg-res
  (is (zero? (run-doo-phantom 'emcg.e2e-runner-add-mcg-res)))

  (let [mcg-entry (get-mcg-entry
                   ;; dupe cljs test value
                   1)]
    (is (not (nil? (:idx-resp mcg-entry))))
    )
  )
