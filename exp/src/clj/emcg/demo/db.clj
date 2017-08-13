(ns emcg.demo.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [emcg.config :refer [db-spec]]
   [emcg.demo.db.thingone :as thingone]
   ))

(defn reset-db! []
  (thingone/drop-table db-spec)
  (thingone/create-table db-spec))

;; returns number of rows inserted
(defn add-a-thingone! [name]
  (first ;; exactly one row is inserted
   (jdbc/execute!
    db-spec
    (thingone/add-a-thing name))
   ))

(defn all-the-thingone-names []
  (map
   :nameone
   (jdbc/query
    db-spec
    (thingone/all-the-names))))
