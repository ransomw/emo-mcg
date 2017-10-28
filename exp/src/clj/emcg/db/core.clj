(ns emcg.db.core
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.clojure-jdbc :as cj-adapter]

   [emcg.db.core-helpers :as h]
   [emcg.db.expone :as exo]
   ))

(defn app-init []
  (hugsql/set-adapter! (cj-adapter/hugsql-adapter-clojure-jdbc)))

(defn reset-db! [db]
  (h/drop-all-tables! (:connection db))
  (h/create-all-tables! (:connection db))
  )

(defn init-exp! [db num-emo-stim]
  (let [exp-id (h/add-exp! (:connection db))]
    (doall ;; force evaluation of lazy seq
     (map (partial h/add-mcg-block! (:connection db))
          (h/add-emo-stims! (:connection db) exp-id num-emo-stim)))
    exp-id))

(defn get-exp [db exp-id]
  (exo/get-exp (:connection db) exp-id))

(defn set-mcg-res! [db mcg-id idx-resp]
  (exo/set-mcg-res (:connection db) mcg-id idx-resp))
