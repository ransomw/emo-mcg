(ns emcg.db.core
  (:require
   [emcg.db.core-helpers :as h]
   [emcg.db.expone :as exo]
   [emcg.config :refer [db-spec]]
   ))

(defn reset-db! []
  (h/drop-all-tables!)
  (h/create-all-tables!)
  )

(defn init-exp! [num-emo-stim]
  (let [exp-id (h/add-exp!)]
    (doall ;; force evaluation of lazy seq
     (map h/add-mcg-block!
          (h/add-emo-stims! exp-id num-emo-stim)))
    exp-id))

(defn get-exp [exp-id]
  (exo/get-exp db-spec exp-id))

(defn set-mcg-res! [mcg-id idx-resp]
  (exo/set-mcg-res db-spec mcg-id idx-resp))
