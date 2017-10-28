(ns emcg.db.component
  (:require
   [com.stuartsierra.component :as component]
   [jdbc.core :as jdbc]
   [emcg.db.core :refer [app-init]]
   ))

(defrecord Database [db-spec]
  component/Lifecycle
  (start [component]
    (app-init)
    (let [conn (jdbc/connection (:db-spec component))
          updated-component (assoc component :connection conn)
          ]
      updated-component
      ))
  (stop [component]
    (when-let [conn (:connection component)]
      (.close conn))
    (assoc component :connection nil)))

(defn new-database [db-spec]
  (map->Database {:db-spec db-spec}))
