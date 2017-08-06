(ns emcg.devcards.expone-pieces
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
   [emcg.comp.expone :as eone]
   [emcg.devcards.dat :as dat]
   ))

(defcard-doc
  "pieces")

(defcard
  "*** mcg-comp ***"
  (dc/om-root eone/mcg-comp)
  {:mcg-id 7
   :vid-url "vid/placeholder_V1A1.mp4"
   :av-idxs '(2 0)}
  {:inspect-data true})

(defcard
  "*** emo-comp ***"
  (dc/om-root eone/emo-comp)
  {:emo-id 3
   :vid-url "vid/placeholder_E1.mp4"}
  {:inspect-data true})
