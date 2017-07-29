(ns user
  (:require
   [emcg.server]
   [ring.middleware.reload :refer [wrap-reload]]
   [figwheel-sidecar.repl-api :as figwheel]
   [figwheel-sidecar.config :as figwheel-config]
   [clojure.test :refer [run-tests]]))

;; Let Clojure warn you when it needs to reflect on types, or when it does math
;; on unboxed numbers. In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(def http-handler
  (wrap-reload #'emcg.server/http-handler))

(defn run []
  (figwheel/start-figwheel!
   {:builds (figwheel-config/get-project-builds)
    :ring-handler 'user/http-handler
    :builds-to-start ["app"]}
   ))

(def browser-repl figwheel/cljs-repl)

;;;;;;; added to chestnut

(defn run-devcards []
  (figwheel/start-figwheel!
   {:builds (figwheel-config/get-project-builds)
    :http-server-root "devcards"
    :css-dirs ["resources/public/css"]
    :builds-to-start ["devcards"]}
   ))

(defn stop []
  (figwheel/stop-figwheel!))

(defn require-test []
  (require 'emcg.db-test :reload)
  (require 'emcg.routes-test :reload)
  (require 'emcg.e2e-test :reload)
  )

(defn init-require []
  (require '(emcg [rand :as mr]) :reload)
  (require '(emcg.db [expone :as eo]) :reload)
  (require '(emcg [db :as db]) :reload)
  (require '(emcg [routes :as r]) :reload)

  (require '(clojure.java [io :as io]))

  (require '(emcg [expone :refer [emo-stim-filenames mcg-stim-filenames]
                   :rename {emo-stim-filenames esf
                            mcg-stim-filenames msf}]))
  (require-test)
  )

(init-require)

(defn reload-require []
  (require 'user :reload)
  (require 'emcg.rand :reload)
  (require 'emcg.db.expone :reload)
  (require 'emcg.db :reload)
  (require 'emcg.hroutes :reload)
  (require 'emcg.routes :reload)
  (require-test)
  )

(defn run-all-tests []
  (run-tests 'emcg.db-test)
  (run-tests 'emcg.routes-test)
  (run-tests 'emcg.e2e-test)
  )

(def rreq reload-require)
(def rdb db/reset-db!)
(def brep browser-repl)
(defn rtest [] (do (reload-require) (run-all-tests)))

;;;;;;

(defn enter-ns [namespace]
  (require namespace :reload)
  (in-ns namespace))
