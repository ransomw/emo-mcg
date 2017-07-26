(ns emcg.comm-test
  (:require-macros
   [cljs.test :refer [is deftest testing async]]
   [cljs.core.async.macros :refer [go]]
   )
  (:require
   [cljs.test]
   [cljs.core.async :refer [chan <! >! close!]]
   [cljs-http.client :as http]
   [emcg.comm :as comm]
   ))


(deftest post-false-endpoint-test
  (async
   done
       (go (let [response (<! (http/post
                               "/definite/non/exist/asdfqwerzxcv"
                               {:edn-params {}}))]

             (println "non-exist response")
             (println response)
             (done)
          ))
   )
  )

(deftest create-exp-test

  (println "here")


  (async
   done

   (let [
        res-chan (comm/create-exp)
        ]


     (println "tthere")
     (println res-chan)


     (go (let [{res :res err :err} (<! res-chan)]

           (println "there")
           (println res)
           (println err)

           (done)
           ))
     )
    )

  )
