(ns emcg.routes.expone
  (:require
   [clojure.java.io :as io]
   [clojure.set :refer [rename-keys]]
   [compojure.core :refer
    [ANY GET PUT POST DELETE
     routes defroutes context]]
   [compojure.route :refer [resources]]
   [compojure.coercions :refer [as-int]]
   [emcg.routes.helpers
    :refer [load-static-asset
            make-edn-resp]]
   [emcg.db.core :as db]
   [emcg.routes.expone-munge :as m]
   ))

(def ^:dynamic *num-emo-stims* 2)

(defn make-stim-resp [filename]
  (let [asset-path (str "assets/emcg/vid/" filename)]
    {:status 200
     :headers {"Content-Type" "video/mp4"} ;; "video/mp4; charset=utf-8"
     :body (load-static-asset asset-path)
     }))

(defn routes-expone [{db :db}]
  (routes
   ;; (GET "/exp/:id" [id] (db/get-exp id))
   (POST "/exp" _
         (let [exp-id (db/init-exp! db *num-emo-stims*)]
           (if exp-id
             (make-edn-resp
              {:id exp-id
               :defn (m/shape-expone-data
                      (db/get-exp db exp-id))})
             (make-edn-resp
              {:msg "init epxeriment databse fail"
               :data {:num-emo-stims *num-emo-stims*}}
              500))))

   (GET "/exp/:exp-id/emo/:emo-id"
        [exp-id :<< as-int emo-id :<< as-int]
        (let [emo-filename (m/get-emo-filename (db/get-exp db exp-id)
                                               emo-id)]
          (if emo-filename
            (make-stim-resp emo-filename)
            ;; todo: replace with "file not found"-like response
            (make-edn-resp
             {:msg (str "emo data lookup failed: none or too many "
                        "entries for this exp/emo id pair")
              :data {:exp-id exp-id :emo-id emo-id}}
             400))
          ))

   (GET "/exp/:exp-id/emo/:emo-id/mcg/:mcg-id"
        [exp-id :<< as-int emo-id :<< as-int mcg-id :<< as-int]
        (let [mcg-filename (m/get-mcg-filename (db/get-exp db exp-id)
                                             emo-id mcg-id)]
          (if mcg-filename
            (make-stim-resp mcg-filename)
            ;; todo: replace with "file not found"-like response
            (make-edn-resp
             {:msg (str "mcg data lookup failed: none or too many "
                        "entries for this exp/emo/mcg id tuple")
              :data {:exp-id exp-id :emo-id emo-id :mcg-id mcg-id}}
             400))))

   (POST "/exp/:exp-id/emo/:emo-id/mcg/:mcg-id/resp"
         [exp-id :<< as-int emo-id :<< as-int
          mcg-id :<< as-int
          ;; compojure coercions only apply to strings
          idx-resp]
         (if (not (nil? (db/set-mcg-res! db mcg-id idx-resp)))
           (make-edn-resp {})
           (make-edn-resp
            {:msg "failed to store mcg resp"
             :data {:exp-id exp-id :emo-id emo-id :mcg-id mcg-id
                    :idx-resp idx-resp}}
            400))
         )
   ))
