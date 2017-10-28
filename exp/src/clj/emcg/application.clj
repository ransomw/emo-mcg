(ns emcg.application
  (:gen-class)
  (:require
   [com.stuartsierra.component :as component]
   [system.components.endpoint :refer [new-endpoint]]
   [system.components.handler :refer [new-handler]]
   [system.components.middleware :refer [new-middleware]]
   [system.components.http-kit :refer [new-web-server]]

   [emcg.db.component :refer [new-database]]
   [emcg.routes.core :refer [routes-main]]
   [emcg.config :refer [config]]
   ))

(defn app-system [config]
  (component/system-map
   :db (new-database (:db-spec config))
   :routes
   (-> (new-endpoint routes-main)
       (component/using [:db]))
   :middleware
   (new-middleware {:middleware (:middleware config)})
   :handler
   (-> (new-handler)
       (component/using [:routes :middleware]))
   :http
   (-> (new-web-server (:http-port config))
       (component/using [:handler]))
   ))


(defn -main [& _]
  (let [config (config)]
    (-> config
        app-system
        component/start)
    (println "Started emcg on"
             (str "http://localhost:" (:http-port config)))
    ))