(ns emcg.server
  (:require
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   [ring.middleware.logger :refer [wrap-with-logger]]
   [environ.core :refer [env]]
   [ring.adapter.jetty :refer [run-jetty]]
   [emcg.routes.core :refer [routes-main]]
   [ring.middleware.format :refer [wrap-restful-format]]
   )
  (:gen-class))

(def http-handler
  (-> routes-main
      (wrap-defaults api-defaults)
      (wrap-restful-format :format [:edn])
      wrap-with-logger
      wrap-gzip
      ))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})))
