(ns emcg.config
  (:require
   [environ.core :refer [env]]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   [ring.middleware.logger :refer [wrap-with-logger]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [ring.middleware.cors :refer [wrap-cors]]
   ))

(defn config [& {:keys [doo-test]}]
  {:http-port (Integer. (or (env :port) 10555))
   :middleware
   (->> [[wrap-defaults api-defaults]
         [wrap-restful-format :format [:edn]]
         ;; wrap-with-logger
         (if (not doo-test) wrap-gzip)
         (if doo-test wrap-cors)
         ]
        (filter identity))
   :db-spec
   {:vendor "postgresql"
    :name "emcg"
    :host "localhost"
    :user "sandy"
    :password "abc123"
    }
   })
