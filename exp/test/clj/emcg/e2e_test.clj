(ns emcg.e2e-test
  (:require
   [clojure.string :as s]
   [clojure.test :refer :all]
   [com.stuartsierra.component :as component]
   [doo.core :as doo]
   [cljs.build.api :as cljs]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.cors :refer [wrap-cors]]

   [emcg.application]
   [emcg.config :refer [config]]
   [emcg.db.core :as db]
   [emcg.db.expone :as eone]
   ))

(defn test-system []
  (emcg.application/app-system
   ;; (config)
   (config :doo-test true)
   )
  )

(def ^{:dynamic true} *db*)

;; (def http-handler-cors
;;   (-> http-handler
;;       (wrap-cors
;;        :access-control-allow-origin [#".*"]
;;        :access-control-allow-methods [:get :put :post :delete])
;;       ))


(use-fixtures :each
  (fn [f]
    (let [system (component/start (test-system))]
      (binding [*db* (:db system)]
        (db/reset-db! *db*)
        (f))
      (component/stop system))
    ))

(defn get-mcg-entry [mcg-id]
  (eone/get-mcg-entry (:connection *db*) mcg-id))

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
  (is (not (= 0 (count (db/get-exp *db* 1)))))
  (is (= 0 (count (db/get-exp *db* 0))))
  )

(deftest e2e-suite-add-mcg-res
  (is (zero? (run-doo-phantom 'emcg.e2e-runner-add-mcg-res)))

  (let [mcg-entry (get-mcg-entry
                   ;; dupe cljs test value
                   1)]
    (is (not (nil? (:idx-resp mcg-entry))))
    )
  )
