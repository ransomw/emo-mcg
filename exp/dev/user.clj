(ns user
  (:require
   [emcg.server]
   [ring.middleware.reload :refer [wrap-reload]]
   [figwheel-sidecar.repl-api :as figwheel]
   [figwheel-sidecar.config :as figwheel-config]
   [clojure.test :refer [run-tests test-vars]]
   [clojure.java.io :as io]
   ))

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

(defn build-cljs []
  (figwheel/clean-builds :app :devcards))

(defn stop []
  (figwheel/stop-figwheel!))

(defn require-test []
  (require 'emcg.db-test :reload)
  (require 'emcg.routes-test :reload)
  (require 'emcg.e2e-test :reload)
  )

(defn init-require []
  (require '(emcg [util :as util]) :reload)
  (require '(emcg.db [expone :as eo]) :reload)
  (require '(emcg.db [core :as db]) :reload)
  (require '(emcg [expone :refer [emo-stim-filenames mcg-stim-filenames]
                   :rename {emo-stim-filenames esf
                            mcg-stim-filenames msf}]))
  (require-test)
  )

(init-require)

(defn reload-require []
  (require 'user :reload)
  (require 'emcg.util :reload)
  (require 'emcg.db.expone :reload)
  (require 'emcg.db.core :reload)
  (require 'emcg.hroutes :reload)
  (require 'emcg.routes.core :reload)
  (require-test)
  )

(defn run-all-tests []
  (run-tests 'emcg.db-test)
  (run-tests 'emcg.routes-test)
  (run-tests 'emcg.e2e-test)
  )

(defn run-passing-e2e-tests []
  (test-vars [
              #'emcg.e2e-test/e2e-suite-no-db
              #'emcg.e2e-test/e2e-suite-create-exp
              #'emcg.e2e-test/e2e-suite-add-mcg-res-no-db
              #'emcg.e2e-test/e2e-suite-add-mcg-res
              ]
   ))

(defn run-failing-e2e-tests []
  (test-vars []))

(def rreq reload-require)
(def rdb db/reset-db!)
(def brep browser-repl)
(defn rtest [] (do (reload-require) (run-all-tests)))

;;;;;;

(defn enter-ns [namespace]
  (require namespace :reload)
  (in-ns namespace))
