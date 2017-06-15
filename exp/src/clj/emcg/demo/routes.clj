(ns emcg.demo.routes
  (:require
   [clojure.java.io :as io]
   [compojure.core :refer
    [ANY GET PUT POST DELETE
     routes defroutes context]]
   [compojure.route :refer [resources]]

   [emcg.hroutes :refer
    [
     load-static-asset
     make-edn-resp
     ]
    :rename
    {
     load-static-asset load-static-asset-global
     }]
   [emcg.demo.db :as db]
   ))

(defn my-print [something]
  (.println System/out (str something)))

(defn load-static-asset [path]
  (load-static-asset-global (str "assets/demo/" path)))

(defn handle-thing-one [name]
  (do
    (let [new-id
          (db/add-a-thingone! name)]
      (make-edn-resp
       {:new-id new-id})
      )))

(defn routes-def []
  (routes
   (GET "/hello" []
        "Hello, demo..")
   (GET "/vid" []
        {:status 200
         :headers {"Content-Type"
                   ;; "video/mp4; charset=utf-8"
                   "video/mp4"
                   }
         :body (load-static-asset
                "vid/hello_parking_meter.mp4")})
   (GET "/pic" []
        {:status 200
         :headers {"Content-Type"
                   ;; "image/jpeg; charset=utf-8"
                   "image/jpeg"
                   }
         :body (load-static-asset
                "img/simp_sea_capn_01.jpg")})
   (GET "/thingone" _
        (make-edn-resp (db/all-the-thingone-names)))
   (POST "/thingone" [name] ;;
         (handle-thing-one name))
   ))
