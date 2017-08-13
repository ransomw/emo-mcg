(ns emcg.act
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [clojure.set :refer [rename-keys]]
   [cljs.core.async :refer [<! >! close!]]
   [emcg.util :refer [count-map-lists]]
   [emcg.state.core :refer [app-state update-chan]]
   [emcg.state.data-munge :refer [get-mcg-idx-in-block
                                  get-app-state-stim-id-head
                                  count-stims]]
   [emcg.comm :as comm]
   ))


(defn fetch-stims [exp-def]
  (let [res-chan (comm/fetch-stims exp-def)]
    (go
      (dotimes
          [_ (count-stims exp-def)]
        (let [{res :res err :err} (<! res-chan)]
          (if res
            (cond
              (:emo-id res) (>! update-chan
                                {:emo-stim
                                 (rename-keys res {:emo-id :id})})
              (:mcg-id res) (>! update-chan
                                {:mcg-stim
                                 (rename-keys res {:mcg-id :id})})
              :else (do
                      (print "neither emo or mcg id present"
                             "on fetch-stim result:" res)
                      (>! update-chan {:init-err true})))
            (do
              (print "didn't get response fetching stim."
                     "error:" err)
              (>! update-chan {:init-err true})))))
      (close! res-chan))
    nil))

(defn create-exp []
  (let [res-chan (comm/create-exp)]
    (go (let [{res :res err :err} (<! res-chan)]
          (if res
            (do (>! update-chan {:exp-def res})
                (fetch-stims res))
            (do
              (print "didn't get response creating experiment."
                     "error:" err)
              (>! update-chan {:init-err true})))
          (close! res-chan)))
    nil
    ))

(defn add-emo-res [emo-id]
  (do
    (go (>! update-chan {:emo-res {:id emo-id}}))
    nil))

(defn add-mcg-res [mcg-id-param {idx-resp :av-idx :as click-data}]
  (let [
        app-state-curr (deref app-state)
        {{exp-id :id} :exp-def} app-state-curr
        {:keys [emo-id mcg-id]
         } (get-app-state-stim-id-head app-state-curr)
        res-chan (comm/add-mcg-res exp-id emo-id mcg-id idx-resp)
        ]
    (go
      (if (not (= mcg-id mcg-id-param))
        nil) ;; todo: warn? error?
      (>! update-chan {:mcg-res {:id mcg-id-param
                                 :click-data click-data}})
      (let [{res :res err :err} (<! res-chan)]
        ;; todo: check errors.  in particular,
        ;; handle momentary disconnects
        nil)
      (close! res-chan))
    nil))
