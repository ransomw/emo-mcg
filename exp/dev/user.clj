(ns user
  (:require
   [clojure.repl :refer [doc]]
   [clojure.test :refer [run-tests test-vars]]
   [clojure.java.io :as io]
   [clojure.tools.namespace.repl
    :refer [set-refresh-dirs refresh refresh-all]]
   [reloaded.repl :refer [system init]]
   [com.stuartsierra.component :as component]
   [figwheel-sidecar.repl-api :as figwheel]
   [figwheel-sidecar.config :as fw-config]
   [figwheel-sidecar.system :as fw-sys]

   [emcg.application]
   [emcg.config :refer [config]]

   [emcg.db-test]
   [emcg.routes-test]
   [emcg.e2e-test]
   ))

;; Let Clojure warn you when it needs to reflect on types,
;; or when it does math on unboxed numbers.
;; In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn run []
  (figwheel/start-figwheel!
   {:builds (fw-config/get-project-builds)
    :ring-handler 'user/http-handler
    :builds-to-start ["app"]}
   ))

(def browser-repl figwheel/cljs-repl)

;;;;;;; added to chestnut

(defn dev-system []
  (assoc (emcg.application/app-system (config))
    :figwheel-system (fw-sys/figwheel-system (fw-config/fetch-config))
    :css-watcher (fw-sys/css-watcher
                  {:watch-paths ["resources/public/css"]})
    ))

(defn devcards-system []
  (assoc
   (emcg.application/app-system (config))
   :figwheel-system
   (fw-sys/figwheel-system
    (-> (fw-config/fetch-config)
        (update-in
         [:data :figwheel-options]
         #(merge % {:http-server-root "devcards"}))
        (update-in
         [:data]
         #(merge % {:build-ids ["devcards"]}))
        )
    )))

(set-refresh-dirs "src" "dev" "test")

(defn go []
  (reloaded.repl/set-init! #(dev-system))
  (reloaded.repl/go))
(defn go-devcards []
  (reloaded.repl/set-init! #(devcards-system))
  (reloaded.repl/go))
(def stop reloaded.repl/stop)

(defn run-all-tests []
  (do
    (stop)
    ;; refresh-all in case of sql changes
    (refresh-all)
    (map run-tests
         [
          'emcg.db-test
          'emcg.routes-test
          'emcg.e2e-test
          ])
    ))

(defn cljs-clean []
  (figwheel/start-figwheel!)
  (figwheel/clean-builds :app :devcards)
  (figwheel/stop-figwheel!)
  )
