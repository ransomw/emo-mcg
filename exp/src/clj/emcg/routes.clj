(ns emcg.routes
  (:require
   [clojure.java.io :as io]
   [clojure.set :refer [rename-keys]]
   [compojure.core :refer
    [ANY GET PUT POST DELETE
     routes defroutes context]]
   [compojure.route :refer [resources]]
   [compojure.coercions :refer [as-int]]

   [emcg.hroutes :refer [load-static-asset make-edn-resp route-print]]
   [emcg.demo.routes :refer [routes-def]
    :rename {routes-def routes-def-demo}]

   [emcg.expone :refer [emo-stim-filenames mcg-stim-filenames]]
   [emcg.db :as db]
   ))

(defn prune-private-expone-emo-data [emo-data]
  (rename-keys
   (update-in
    emo-data [:mcg-trials]
    (fn [mcg-data-list]
      (->>
       mcg-data-list
       (sort-by :seq-num)
       (map #(dissoc % :idx-a :idx-v :seq-num))
       (map :mcg-id)
       )
      ))
  {:mcg-trials :mcg-ids}))

(defn shape-expone-emo-data [emo-data]
  (let [mcg-data-list-sorted
        (->>
         (:mcg-trials emo-data)
         (sort-by :seq-num)
         (map #(dissoc % :seq-num)))]
    (merge
     emo-data
     {:mcg-ids (map :mcg-id mcg-data-list-sorted)}
     {:av-idxs (map list
                    (map :idx-a mcg-data-list-sorted)
                    (map :idx-v mcg-data-list-sorted))})))

(defn prune-private-expone-data [exp]
  (->>
   exp
   ;; (map prune-private-expone-emo-data)
   (map shape-expone-emo-data)
   (sort-by :seq-num)
   (map #(dissoc % :seq-num :emo-idx :mcg-trials))
   )
  )

(defn make-stim-resp [filename]
  (let [asset-path (str "assets/emcg/vid/" filename)]
    {:status 200
     :headers {"Content-Type" "video/mp4"} ;; "video/mp4; charset=utf-8"
     :body (load-static-asset asset-path)
     }))

(defn get-emo-data [exp-id emo-id]
  (let [emo-datas (filter #(= (:emo-id %) emo-id)
                          (db/get-exp exp-id))]
    (if (= 1 (count emo-datas))
      (first emo-datas))))

(defn get-mcg-data [exp-id emo-id mcg-id]
  (let [emo-data (get-emo-data exp-id emo-id)]
    (if emo-data
      (let [mcg-datas (filter #(= (:mcg-id %) mcg-id)
                              (:mcg-trials emo-data))]
        (if (= 1 (count mcg-datas))
          (first mcg-datas))))))


(defn routes-expone []
  (routes
   ;; (GET "/exp/:id" [id] (db/get-exp id))
   (POST "/exp" _ (let [exp-id (db/init-exp! 2)]
                    (make-edn-resp
                     {:id exp-id
                      :defn (prune-private-expone-data
                             (db/get-exp exp-id))})))
   (GET "/exp/:exp-id/emo/:emo-id"
        [exp-id :<< as-int emo-id :<< as-int]
        (let [emo-data (get-emo-data exp-id emo-id)]
          (if emo-data
            (make-stim-resp
             (nth emo-stim-filenames (:emo-idx emo-data)))
            ;; todo: replace with "file not found"-like response
            (make-edn-resp
             {:msg (str "emo data lookup failed: none or too many "
                        "entries for this exp/emo id pair")}
             400))
          ))
   (GET "/exp/:exp-id/emo/:emo-id/mcg/:mcg-id"
        [exp-id :<< as-int emo-id :<< as-int mcg-id :<< as-int]
        (let [mcg-data (get-mcg-data exp-id emo-id mcg-id)]
          (if mcg-data
            (let [{idx-a :idx-a idx-v :idx-v} mcg-data]
              (make-stim-resp
               (-> mcg-stim-filenames
                   (nth idx-v)
                   (nth idx-a))))
            ;; todo: replace with "file not found"-like response
            (make-edn-resp
             {:msg (str "mcg data lookup failed: none or too many "
                        "entries for this exp/emo/mcg id tuple")}
             400))))

   (POST "/exp/:exp-id/emo/:emo-id/mcg/:mcg-id/resp"
         [exp-id :<< as-int emo-id :<< as-int
          mcg-id :<< as-int idx-resp :<< as-int]

        (route-print
         "get for emo stim: exp-id, emo-id, mcg-id, idx-resp")
        (route-print exp-id)
        (route-print emo-id)
        (route-print mcg-id)
        (route-print idx-resp)

        (make-edn-resp {:msg "unimpl"} 500))
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
