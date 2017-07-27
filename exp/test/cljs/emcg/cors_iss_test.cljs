(ns emcg.cors-iss-test
  (:require-macros
   [cljs.test :refer [is deftest testing async]]
   [cljs.core.async.macros :refer [go]]
   )
  (:require
   [clojure.string :as s]
   [cljs.test]
   [cljs.core.async :refer [chan <! >! close!]]
   [cljs-http.client :as http]
   [emcg.config :refer [base-url]]
   [emcg.comm :as comm]
   ))


(deftest upstream-snippet-test

  (println "top of upstream-snippet-test")

  (async
   done
   (go (let [response (<! (http/get
                           "https://developer.mozilla.org/en-US/docs/Web/CSS/line-height?raw&section=Summary"
                           {:with-credentials? false}))
             ]
         (println response)
         (done)
         ))))

(deftest get-index-http-test
  (async
   done
       (go (let [response (<! (http/get
                               ;; (s/join [base-url "/"])
                               "http://localhost:3333"
                               {
                                :with-credentials? false
                                }))]

             (println "index response")
             (println response)
             (done)
          ))
   )
  )
