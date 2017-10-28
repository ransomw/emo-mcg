(ns emcg.routes.core
  (:require
   [compojure.core :refer
    [ANY GET PUT POST DELETE
     routes defroutes context]]
   [compojure.route :refer [resources]]
   [emcg.routes.helpers :refer [load-static-asset]]
   [emcg.routes.expone :refer [routes-expone]]
   ))


(defn routes-main [{db :db :as endpoint}]
  (routes
   (GET "/" _
        {:status 200
         :headers {"Content-Type" "text/html; charset=utf-8"}
         :body (load-static-asset "public/index.html")})
   (context
    "/emcg" _
    (routes-expone {:db db}))
   (resources "/")
   ))
