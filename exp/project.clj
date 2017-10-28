(defproject emcg "0.0.1"
  :description "emotional McGurk effect data collection"
  :dependencies
  [
   ;;;;;;;; from chestnut
   [org.clojure/clojure "1.8.0"]
   [org.clojure/clojurescript "1.9.89" :scope "provided"]
   [ring "1.6.2"]
   [ring/ring-defaults "0.3.1"]
   [ring/ring-mock "0.3.1"]
   [bk/ring-gzip "0.2.1"]
   [ring.middleware.logger "0.5.0"]
   [compojure "1.6.0"]
   [environ "1.1.0"]
   [org.omcljs/om "1.0.0-beta1"]
   ;;;;;;;; added to chestnut
   [com.stuartsierra/component "0.3.2"]
   [org.danielsz/system "0.4.0"]
   [http-kit "2.2.0"]
   ;;;; client
   [cljs-http "0.1.43"]
   ;;;; server
   [postgresql "9.3-1102.jdbc41"]
   [com.layerware/hugsql "0.4.7"]
   [com.layerware/hugsql-adapter-clojure-jdbc "0.4.7"]
   ;; explicitly specify to avoid
   ;;  namespace 'cheshire.factory' not found
   ;; error with ring-middleware-format add
   [cheshire "5.7.0"]
   [ring-middleware-format "0.7.2"]
   [ring-cors "0.1.11"]
   ;; todo: dedupe plugin?
   ;; duplicates plugin version
   [lein-doo "0.1.7"]
   [devcards "0.2.3"]
   [reagent "0.7.0"]
   ]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-environ "1.1.0"]]

  :min-lein-version "2.7.1"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false}
  [:target-path :compile-path
   "resources/public/js"
   "resources/devcards/js"]

  :uberjar-name "emcg.jar"

  ;; for `lein run`
  :main "emcg.application"

  :repl-options
  {:init-ns user
   :timeout 120000 ;; ms 30000 default
   }

  :cljsbuild
  {:builds
   [{:id "app"
     ;; clj test env keeps local db
     ;; cljs production env connects to full backend
     :source-paths ["src/cljs" "src/cljc"
                    "env/prod/cljs" "env/test/clj"
                    ]
     :figwheel true
     ;; Alternatively,
     ;; :figwheel {:on-jsload "emcg.core/on-figwheel-reload"}
     :compiler {:main emcg.core
                :asset-path
                "js/compiled/out"
                :output-to
                "resources/public/js/compiled/emcg.js"
                :output-dir
                "resources/public/js/compiled/out"
                :source-map-timestamp true}}

    {:id "devcards"
     :source-paths ["src/cljs" "src/cljc"
                    "env/test/cljs" "env/test/clj"
                    "test/cljs" "test/cljc"]
     :figwheel {:devcards true}
     :compiler
     {
      :main emcg.devcards
      :asset-path
      "js/compiled/devcards_out"
      :output-to
      ;; matches http-server-root for figwheel
      "resources/devcards/js/compiled/emcg_devcards.js"
      :output-dir
      "resources/devcards/js/compiled/devcards_out"
      :source-map-timestamp true}}

    {:id "test"
     :source-paths ["src/cljs" "test/cljs"
                    "src/cljc" "test/cljc"]
     :compiler {:output-to "resources/public/js/compiled/testable.js"
                :main emcg.test-runner
                :optimizations :none}}

    {:id "min"
     :source-paths ["src/cljs" "src/cljc"
                    "env/prod/cljs"]
     :jar true
     :compiler {:main emcg.core
                :output-to "resources/public/js/compiled/emcg.js"
                :output-dir "target"
                :source-map-timestamp true
                :optimizations :advanced
                :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :server-logfile "log/figwheel.log"}

  :doo {:build "test"}

  :profiles
  {:dev
   {:dependencies [
                   [figwheel "0.5.12"]
                   [figwheel-sidecar "0.5.12"]
                   [com.cemerick/piggieback "0.2.1"]
                   [org.clojure/tools.nrepl "0.2.12"]
                   [reloaded.repl "0.2.3"]
                   ]

    :plugins [
              [lein-figwheel "0.5.12"]
              [lein-doo "0.1.7"]
              ]

    :source-paths ["dev" "env/test/clj"]
    :repl-options {:nrepl-middleware
                   [cemerick.piggieback/wrap-cljs-repl
                    ]}}

   :uberjar
   {:source-paths ^:replace ["src/clj" "src/cljc"
                             "env/test/clj"
                             ]
    :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
    :hooks []
    ;; include .sql files
    :omit-source false
    :aot :all
    }})
