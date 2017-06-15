(ns emcg.hroutes
  (:require
   [clojure.java.io :as io]
   ))

;; helpers for route functions

;; since *out* may not be set appropriately
(defn route-print [something]
  (.println System/out (str something)))

(defn load-static-asset [path]
  (io/input-stream (io/resource
                    path
                    ;; (str "public/" path)
                    )))

(defn make-edn-resp [data & [status]]
  {:status (if status status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})
