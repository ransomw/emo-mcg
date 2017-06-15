(ns emcg.act
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [clojure.set :refer [rename-keys]]
   [cljs.core.async :refer [<! >! close!]]
   [emcg.state :refer [update-chan count-stims]]
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

(defn add-mcg-res [mcg-id click-data]
  (do
    (go (>! update-chan {:mcg-res {:id mcg-id
                                   :click-data click-data}}))
    nil))
