(ns emcg.devcards.re-expone
  (:require-macros
   ;; Notice that I am not including the 'devcards.core namespace
   ;; but only the macros. This helps ensure that devcards will only
   ;; be created when the :devcards is set to true in the build config.
   [devcards.core :as dc :refer
    [defcard defcard-doc noframe-doc deftest dom-node reagent]]
   [cljs.test :refer [is testing]]
   )
  (:require
   [devcards.core]
   [reagent.core :as r]
   [emcg.recomp.root :as root]
   [emcg.devcards.dat :as dat]
   ))

(deftest atomic-tests
  (testing "cljs atom and reagent atom interaction"
    (let [app-state (atom {
                           :init-err nil
                           :exp-def nil
                           :stim-infos {:emo (list) :mcg (list)}
                           :exp-res {:emo (list) :mcg (list)}
                           })
          re-app-state (r/atom @app-state)
          cb-cp
          (fn [k r o n]
            (swap! re-app-state assoc k (k n))
            )
          listener-exp-def
          (add-watch app-state :exp-def cb-cp)
          listener-stim-infos
          (add-watch app-state :stim-infos cb-cp)
          ]
      (is (= @app-state @re-app-state))
      (swap! app-state assoc :exp-def (:exp-def dat/app-state-init))
      (is (= @app-state @re-app-state))
      (swap! app-state update-in [:stim-infos :emo]
             #(conj % "would-be-path-append"))
      (is (= @app-state @re-app-state))
      )
    )
  (testing "cljs atom and reagent atom interaction, slight finess"
    (let [app-state (atom {
                           :init-err nil
                           :exp-def nil
                           :stim-infos {:emo (list) :mcg (list)}
                           :exp-res {:emo (list) :mcg (list)}
                           })
          re-app-state (r/atom @app-state)
          cb-cp
          (fn [k r o n]
            (swap! re-app-state assoc k (k n))
            )
          atom-cp-listeners
          (doall (map #(add-watch app-state % cb-cp) (keys @app-state)))
          ]
      (is (= @app-state @re-app-state))
      (swap! app-state assoc :exp-def (:exp-def dat/app-state-init))
      (is (= @app-state @re-app-state))
      (swap! app-state update-in [:stim-infos :emo]
             #(conj % "would-be-path-append"))
      (is (= @app-state @re-app-state))
      )
    )
  )


(defcard-doc
  "concerning reagent,")

(defcard
  (dc/reagent root/root-component)
  dat/app-state-init)
