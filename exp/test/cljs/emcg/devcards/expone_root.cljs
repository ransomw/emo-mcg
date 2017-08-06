(ns emcg.devcards.expone-root
  (:require-macros
   ;; Notice that I am not including the 'devcards.core namespace
   ;; but only the macros. This helps ensure that devcards will only
   ;; be created when the :devcards is set to true in the build config.
   [devcards.core :as dc :refer
    [defcard defcard-doc defcard-om noframe-doc deftest dom-node]]
   [cljs.test :refer [is testing]]
   )
  (:require
   ;; [cljs.test :as t :include-macros true :refer-macros [testing is]])
   [devcards.core]
   [om.dom :as dom :include-macros true]
   [om.core :as om :include-macros true]
   [emcg.state :refer [count-stims]]
   [emcg.comp.root :as root]
   [emcg.devcards.dat :as dat]
   ))

(defcard-doc
  "as much of the kitchen sink as possible")

(deftest root-component-tests
  (let [app dat/app-state-init]
    (testing "duplicate init logic -- todo: extract/dedupe"
      (is (integer?
           (count-stims (:exp-def app))
           ))
      (is (integer?
           (count (apply concat (vals (:stim-infos app))))
           ))
      (is (not (root/fetching-stims? app)))
      (is
       (=
        :build-comp
        (cond
          (:init-err app) :init-err
          (not (:exp-def app)) :initing-defn
          (root/fetching-stims? app) :fetching-stims
          :else :build-comp
          ))
       ))
    ))

(defcard
  "*** root-component ***"
  (dc/om-root root/root-component)
  dat/app-state-init)
