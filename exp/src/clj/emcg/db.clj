(ns emcg.db
  (:require
   [clojure.java.jdbc :as jdbc]

   [emcg.db.expone :as exo]
   [emcg.rand :refer [rand-multilist]]
   ))

(def db-spec
  {:dbtype "postgresql"
   :dbname "emcg"
   :host "localhost"
   :user "sandy"
   :password "abc123"
   })

;; needs database connection as well as ups/downs from
;; hugsql-generated functions
(defn reset-db! []
  ;; todo: cascade delete in sql
  (exo/drop-table-mcg db-spec)
  (exo/drop-table-emo db-spec)
  (exo/drop-table-exp db-spec)
  ;; todo: consolidate UP after hugsql api review
  (exo/create-table-exp db-spec)
  (exo/create-table-emo db-spec)
  (exo/create-table-mcg db-spec)
  )

;;;;;;;;;;;;;;;;; exp api wip

(defn add-exp! []
  (exo/add-exp db-spec))

(defn add-emo-stims! [exp-id num-emo-stim]
  ((partial exo/add-emo-stims db-spec)
   exp-id num-emo-stim))

(defn add-mcg-blocks! [emo-stim-id]
  (exo/add-mcg-stim-block db-spec emo-stim-id))

(defn init-exp! [num-emo-stim]
  (let [exp-id (add-exp!)]
    (doall ;; force evaluation of lazy seq
     (map add-mcg-blocks!
          (add-emo-stims! exp-id num-emo-stim)))
    exp-id))

(defn get-exp [exp-id]
  (exo/get-exp db-spec exp-id))
