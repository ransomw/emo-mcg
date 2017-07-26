(ns emcg.e2e-test
  (:require
   [clojure.test :refer :all]
   [doo.core :as doo]
   [cljs.build.api :as cljs]
   [ring.middleware.cors :refer [wrap-cors]]

   ;; ;; These will be used in the following steps
   ;; [com.stuartsierra.component :as component]
   ;; [my-app.system :as system]


   ))

(deftest end-to-end-suite
  (let [compiler-opts {:main 'my-app.e2e-runner
                       :output-to "out/test.js"
                       :output-dir "out"
                       :asset-path "out"
                       :optimizations :none}]
    ;; Compile the ClojureScript tests
    (cljs/build (apply cljs/inputs ["src/cljs" "test/cljs" "test/cljs-app-config"]) compiler-opts)
    ;; Run the ClojureScript tests and check the result
    (is (zero? (:exit (doo/run-script :phantom compiler-opts {}))))))
