(ns emcg.comm-test
  (:require-macros
   [cljs.test :refer [is deftest testing async]]
   [cljs.core.async.macros :refer [go]]
   )
  (:require
   [cljs.test]
   [cljs.core.async :refer [chan <! >! close!]]
   [cljs-http.client :as http]
   [emcg.type-checks-testing :refer [check-exp-defn]]
   [emcg.comm :as comm]
   ))

(deftest create-exp-test
  (async
   done
   (let [res-chan (comm/create-exp)]
     (go (let [{
                {exp-id :id exp-defn :defn} :res
                err :err} (<! res-chan)]
           (is (integer? exp-id))
           (check-exp-defn exp-defn)
           (done)
           )))))
