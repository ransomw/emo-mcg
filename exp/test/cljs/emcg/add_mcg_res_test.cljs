(ns emcg.add-mcg-res-test
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
   [emcg.state :as st]
   [emcg.comp.root :as root]
   [emcg.comp.expone :as eone]
   ))

(def create-exp-timeout-ms 2500)
;; add-mcg-res updates browser state only
(def add-emo-res-timeout-ms 500)
;; add-mcg-res does not block on http communication
(def add-mcg-res-timeout-ms 500)

(deftest add-res-test
  (async
   done
   (go
     (act/create-exp)
     (<! (timeout create-exp-timeout-ms))
     (let [exp-comp-props (root/app-to-exp-comp-props @st/app-state)]
       (is (eone/emo-stim? exp-comp-props))
       (let [{:keys [emo-id]} (eone/get-emo-props exp-comp-props)]
         (is (integer? emo-id))
         (act/add-emo-res emo-id)))
     (<! (timeout add-emo-res-timeout-ms))

     (let [exp-comp-props (root/app-to-exp-comp-props @st/app-state)]
       (is (not (eone/emo-stim? exp-comp-props)))
       (let [{mcg-id :mcg-id [av-idx1 av-idx2] :av-idxs}
             (eone/get-some-mcg-props exp-comp-props)]
         (is (integer? mcg-id))
         (is (integer? av-idx1))
         (is (integer? av-idx2))
         (act/add-mcg-res mcg-id {:av-idx av-idx1})))
     (<! (timeout add-mcg-res-timeout-ms))
     (done))))
