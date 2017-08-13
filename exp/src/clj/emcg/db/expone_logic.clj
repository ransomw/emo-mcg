(ns emcg.db.expone-logic
  (:require
   [clojure.string :as s]
   [clojure.set :as set]
   [hugsql.core :as hugsql]
   [emcg.util :refer [partition-list-of-maps]]
   [emcg.db.expone-hug :as hug]
   [emcg.expone :refer [exp-stim-config]]
   [emcg.util :refer [rand-multilist]]
   ))

;; todo: don't allow same index for both A and V
(defn rand-idxs-av [num-mcg-stim]
  (map (fn [idx-av]
         {:idx-a (first idx-av)
          :idx-v (last idx-av)})
       (map list
            (rand-multilist num-mcg-stim (range num-mcg-stim))
            (rand-multilist num-mcg-stim (range num-mcg-stim))
            )))

;; create the data for a McGuirk stimulus block,
;; a list of maps with keys
;; :seq-num
;; :idx-a
;; :idx-v
(defn gen-mcg-stim-block []
  (let [idxs-av (rand-idxs-av (:num-mcg exp-stim-config))]
    (map #(merge {:seq-num %1} %2)
         (range (count idxs-av)) idxs-av)))
