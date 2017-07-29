(ns emcg.type-checks-testing
  (:require-macros
   [cljs.test :refer [is]]
   )
  (:require
   [cljs.test]
   ))

(defn check-exp-defn [exp-defn]
  (is (seq? exp-defn))
  (is (= (set '(true))
         (set (map #(= % (set (list
                               :emo-id
                               :mcg-ids
                               :av-idxs
                               )))
                   (map (comp set keys) exp-defn)))))
  )
