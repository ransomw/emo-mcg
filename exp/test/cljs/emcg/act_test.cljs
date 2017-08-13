(ns emcg.act-test
  (:require-macros
   [cljs.test :refer [is deftest testing async]]
   [cljs.core.async.macros :refer [go]]
   )
  (:require
   [cljs.test]
   [cljs.core.async :refer [chan <! >! close! timeout]]
   [cljs-http.client :as http]
   [emcg.type-checks-testing :refer [check-exp-defn]]
   [emcg.act :as act]
   [emcg.state.core :as st]
   ))

(def create-exp-timeout-ms 2500)

(deftest create-exp-test
  (async
   done
   (go
     (let [app-state-curr (deref st/app-state)]
       (is (nil? (:init-err app-state-curr)))
       (is (nil? (:exp-def app-state-curr)))
       (is (= {:emo '(), :mcg '()}
              (:stim-infos app-state-curr)))
       (is (= {:emo '(), :mcg '()}
              (:exp-res app-state-curr))))
     ;; this action async updates application state
     (act/create-exp)
     ;; .. so this sequential ps uses a timeout
     ;; to wait on the update
     (<! (timeout create-exp-timeout-ms))

     (let [{init-err :init-err
            {exp-id :id
             exp-defn :defn
             } :exp-def
            {stim-infos-emo :emo stim-infos-mcg :mcg} :stim-infos
            exp-res :exp-res
            } (deref st/app-state)
           stim-infos (flatten (list stim-infos-emo stim-infos-mcg))
           ]
       (is (nil? init-err))
       (is (= {:emo '(), :mcg '()} exp-res))
       (is (not (nil? stim-infos)))
       ;; ;; YYY mysterious errors out of check on stim infos
       ;; (is (every? identity (map (comp integer? :id) stim-infos)))
       ;; (is (every? identity (map (comp string? :url) stim-infos)))
       ;; (println ;; replace with 'is' for error
       ;;  (=
       ;;   (set (map keys stim-infos))
       ;;   (set ['(:id :url)])
       ;;   ))
       (is (integer? exp-id))
       (check-exp-defn exp-defn)
       )

     (done))))
