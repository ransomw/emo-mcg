(ns emcg.db.core-helpers
  (:require
   [emcg.db.expone :as exo]
   ))

(defn drop-all-tables! [db-conn]
  (exo/drop-table-mcg db-conn)
  (exo/drop-table-emo db-conn)
  (exo/drop-table-exp db-conn)
  )

(defn create-all-tables! [db-conn]
  (exo/create-table-exp db-conn)
  (exo/create-table-emo db-conn)
  (exo/create-table-mcg db-conn)
  )

(defn add-exp! [db-conn]
  (exo/add-exp db-conn))

(defn add-emo-stims! [db-conn exp-id num-emo-stim]
  (exo/add-emo-stims db-conn exp-id num-emo-stim))

(defn add-mcg-block! [db-conn emo-stim-id]
  (exo/add-mcg-stim-block db-conn emo-stim-id))
