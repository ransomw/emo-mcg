(ns emcg.devcards
  (:require-macros
   ;; Notice that I am not including the 'devcards.core namespace
   ;; but only the macros. This helps ensure that devcards will only
   ;; be created when the :devcards is set to true in the build config.
   [devcards.core :as dc :refer
    [defcard defcard-doc defcard-om noframe-doc deftest dom-node]]
   )
  (:require
   [devcards.core]
   [om.dom :as dom :include-macros true]
   [om.core :as om :include-macros true]
   [emcg.comp.root :as root]
   [emcg.comp.expone :as eone]
   ))

(defcard-doc
  "as much of the kitchen sink as possible")

(defcard
  (dc/om-root root/root-component)
  {:init-err nil
   :exp-def
   {:id 2
    :defn
    '({:emo-id 3 :mcg-ids '(7 8 9) :av-idxs '((2 0) (0 2) (1 1))}
      {:emo-id 4 :mcg-ids '(10 11 12) :av-idxs '((1 1) (0 2) (2 0))})}
   :stim-infos
   {:emo
    '({:id 4 :url "vid/placeholder_E1.mp4"}
      {:id 3 :url "vid/placeholder_E1.mp4"})
    :mcg
    '({:id 12 :url "vid/placeholder_V1A1.mp4"}
      {:id 11 :url "vid/placeholder_V1A1.mp4"}
      {:id 8 :url "vid/placeholder_V1A1.mp4"}
      {:id 10 :url "vid/placeholder_V1A1.mp4"}
      {:id 9 :url "vid/placeholder_V1A1.mp4"}
      {:id 7 :url "vid/placeholder_V1A1.mp4"})}
   :exp-res {:emo '() :mcg '()}})

(defcard-doc
  "... and scaled back as the above is brokenz ...")

(defcard
  (dc/om-root eone/mcg-comp)
  {:mcg-id 7
   :vid-url "vid/placeholder_V1A1.mp4"
   :av-idxs '(2 0)})

(defcard
  (dc/om-root eone/mcg-comp)
  {:emo-id 3
   :vid-url "vid/placeholder_E1.mp4"})


(defcard-doc
  "... then rebuilt again .?.?.")

(defcard
  (dc/om-root eone/exp-comp)
  {:exp-def
   {:id 2
    :defn
    '({:emo-id 3 :mcg-ids '(7 8 9) :av-idxs '((2 0) (0 2) (1 1))}
      {:emo-id 4 :mcg-ids '(10 11 12) :av-idxs '((1 1) (0 2) (2 0))})}
   :stim-infos
   {:emo
    '({:id 4 :url "vid/placeholder_E1.mp4"}
      {:id 3 :url "vid/placeholder_E1.mp4"})
    :mcg
    '({:id 12 :url "vid/placeholder_V1A1.mp4"}
      {:id 11 :url "vid/placeholder_V1A1.mp4"}
      {:id 8 :url "vid/placeholder_V1A1.mp4"}
      {:id 10 :url "vid/placeholder_V1A1.mp4"}
      {:id 9 :url "vid/placeholder_V1A1.mp4"}
      {:id 7 :url "vid/placeholder_V1A1.mp4"})}
   :num-res 0})

