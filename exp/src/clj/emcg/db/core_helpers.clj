(ns emcg.db.core-helpers
  (:require
   [emcg.db.expone :as exo]
   [emcg.config :refer [db-spec]]
   ))

(defn drop-all-tables! []
  (exo/drop-table-mcg db-spec)
  (exo/drop-table-emo db-spec)
  (exo/drop-table-exp db-spec)
  )

(defn create-all-tables! []
  (exo/create-table-exp db-spec)
  (exo/create-table-emo db-spec)
  (exo/create-table-mcg db-spec)
  )

(defn add-exp! []
  (exo/add-exp db-spec))

(defn add-emo-stims! [exp-id num-emo-stim]
  (exo/add-emo-stims db-spec exp-id num-emo-stim))

(defn add-mcg-block! [emo-stim-id]
  (exo/add-mcg-stim-block db-spec emo-stim-id))
