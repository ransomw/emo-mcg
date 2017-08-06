(ns emcg.devcards.expone
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
   [emcg.util :refer [count-map-lists]]
   [emcg.state :refer [count-stims]]
   [emcg.data-munge :refer [get-mcg-idx-in-block]]
   [emcg.comp.root :as root]
   [emcg.comp.expone :as eone]
   [emcg.devcards.dat :as dat]
   ))

(deftest dat-tests
  (testing "misc. checks on test data setup"
    (let [mcg-res-path [:exp-res :mcg]]
      (is (not (= dat/app-state-init dat/app-state-one-emo)))
      (is (= (dissoc dat/app-state-init :exp-res)
             (dissoc dat/app-state-one-emo :exp-res)))
      (is (not (= '() (get-in dat/app-state-one-block
                              mcg-res-path))))
      (is (not (= 0 (count (get-in dat/app-state-one-block
                                   mcg-res-path)))))
      (is (> (count (get-in
                     dat/app-state-one-block-one-mcg
                     mcg-res-path))
             (count (get-in
                     dat/app-state-one-block
                     mcg-res-path))
             ))
      )))

(deftest comp-logic-tests
  (testing "misc root component logics"
    (let [map-of-lists {:some-key (list 1 2 3)}]
      (is (= 3 (:some-key (count-map-lists map-of-lists))))
      (is (= 1 (:emo (count-map-lists
                      (:exp-res dat/app-state-one-mcg)))))
      )
    ))

(defcard-doc
  "and back to the top")

(let [app-state dat/app-state-init]
  (deftest init-exp-comp-tests
    (testing "checks on setup data + root component logic"
      (is (= 0 (get-in (root/app-to-exp-comp-props app-state)
                       [:num-res :emo])))
      (is (= 0 (get-in (root/app-to-exp-comp-props app-state)
                       [:num-res :mcg])))
      ))

  (defcard
    "*** exp-comp ( no clicks ) ***"
    (dc/om-root eone/exp-comp)
    (root/app-to-exp-comp-props app-state)
    {:inspect-data true}))

(let [app-state dat/app-state-one-emo
      exp-comp-props (root/app-to-exp-comp-props app-state)]
  (deftest one-emo-exp-comp-tests

    (testing "checks on setup data + root component logic"
      (is (= 1 (get-in exp-comp-props
                       [:num-res :emo])))
      (is (= 0 (get-in exp-comp-props
                       [:num-res :mcg])))
      )

    (testing "the mcg comp will be rendered"
      (is (not (eone/emo-stim? exp-comp-props)))
      )

    (testing "check data munge functions for errors"
      (is (not (nil? (get-mcg-idx-in-block exp-comp-props))))
      (is (not (nil? (eone/get-some-mcg-props exp-comp-props))))
      )


    ;;   (testing "why no tests?"
    ;; (is true))
    ;; (testing "correct av incides on the buttons -- todo: dedupe data"
    ;;     (let [{:keys [exp-def stim-infos num-res]} exp-comp-props
    ;;           mcg-idx-in-block
    ;;           (get-mcg-idx-in-block exp-comp-props)]
    ;;       (is (= 0 mcg-idx-in-block))
    ;;       )
    ;;     ;; todo: dedupe here
    ;;     (is (= {:mcg-id 7 :av-idxs '(2 0)}
    ;;            (eone/get-some-mcg-props exp-comp-props)))
    ;; )

    )

  (defcard
    "*** exp-comp ( one emo click ) ***"
    (dc/om-root eone/exp-comp)
    exp-comp-props
    {:inspect-data true}))

(let [app-state dat/app-state-one-mcg
      exp-comp-props (root/app-to-exp-comp-props app-state)
      ]
  (deftest one-mcg-exp-comp-tests
    (testing "misc. checks on test data setup"
      (is (= 1 (get-in exp-comp-props [:num-res :emo])))
      )
    (testing "checks on setup data + root component logic"
      (is (= 1 (get-in exp-comp-props
                       [:num-res :emo])))
      (is (= 1 (get-in exp-comp-props
                       [:num-res :mcg])))
      )
    (testing "checks that the mcg comp will be rendered"
      (is (not (eone/emo-stim? exp-comp-props)))
      )
    (testing "check data munge functions for errors"
      (is (not (nil? (eone/get-some-mcg-props exp-comp-props))))
      )
    )

  (defcard
    "*** exp-comp ( one mcg click ) ***"
    (dc/om-root eone/exp-comp)
    exp-comp-props
    {:inspect-data true}))

(let [app-state dat/app-state-one-block
      exp-comp-props (root/app-to-exp-comp-props app-state)]
  (deftest one-block-exp-comp-tests
    (testing "checks on setup data + root component logic"
      (is (= 1 (get-in (root/app-to-exp-comp-props
                        app-state) [:num-res :emo])))
      (is (not (= 0 (get-in (root/app-to-exp-comp-props
                             app-state) [:num-res :mcg]))))
      )
    (testing "checks that the emo comp will be rendered"
      (let [{:keys [exp-def stim-infos num-res] :as props-exp-comp}
            exp-comp-props]
        (is (eone/emo-stim? props-exp-comp))
        )
      ))

  (defcard
    "*** exp-comp ( one block:  an emo stim + several mcg stims ) ***"
    (dc/om-root eone/exp-comp)
    exp-comp-props
    {:inspect-data true}))

(let [app-state dat/app-state-one-block-one-emo
      exp-comp-props (root/app-to-exp-comp-props app-state)]

  (defcard
    "*** exp-comp ( one block, one emo responses ) ***"
    (dc/om-root eone/exp-comp)
    exp-comp-props
    {:inspect-data true}))

(let [app-state dat/app-state-one-block-one-mcg
      exp-comp-props (root/app-to-exp-comp-props app-state)]
  (deftest one-block-one-mcg-exp-comp-tests

    (testing "misc. checks on test data setup"
      (is (= 2 (get-in exp-comp-props [:num-res :emo])))
      )

    (testing "the mcg comp will be rendered"
      (is (not (eone/emo-stim? exp-comp-props)))
      )
    (testing "check data munge functions for errors"
      (is (not (nil? (get-mcg-idx-in-block exp-comp-props))))
      (is (not (nil? (eone/get-some-mcg-props exp-comp-props))))
      )
    )

  (defcard
    "*** exp-comp ( one block, one emo, one mcg response ) ***"
    (dc/om-root eone/exp-comp)
    exp-comp-props
    {:inspect-data true}))


(let [app-state dat/app-state-one-block-two-mcg
      exp-comp-props (root/app-to-exp-comp-props app-state)]

  (defcard
    "*** exp-comp ( one block, one emo, two mcg responses ) ***"
    (dc/om-root eone/exp-comp)
    exp-comp-props
    {:inspect-data true}))
