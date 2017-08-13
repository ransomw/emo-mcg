(ns emcg.routes.core
  (:require
   [compojure.core :refer
    [ANY GET PUT POST DELETE
     routes defroutes context]]
   [compojure.route :refer [resources]]
   [emcg.hroutes :refer [load-static-asset make-edn-resp route-print]]
   [emcg.demo.routes :refer [routes-def]
    :rename {routes-def routes-def-demo}]
   [emcg.routes.expone :refer [routes-expone]]
   ))


(defroutes routes-main
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (load-static-asset "public/index.html")})
  (context "/demo" []
    (routes-def-demo))
  (context "/emcg" []
    (routes-expone))
  ;; (resources "/")
  )
