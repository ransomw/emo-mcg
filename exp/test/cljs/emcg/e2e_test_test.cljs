(ns emcg.e2e-test-test
  (:require
   [cljs.test :refer-macros
    [async deftest is testing use-fixtures]]
   [reagent.core :as reagent]
   [emcg.e2e-test.main :as main]
   [cljs-react-test.simulate :as sim]
   [cljs-react-test.utils :as tu]
   ))

(def container (atom nil))

(use-fixtures :each
  {:before #(async done
              (reset! container (tu/new-container!))
              (done))
   :after #(tu/unmount! @container)})

(deftest start-the-app
  (async
   done
   ;; Start the app
   (main/render-app @container)
   (.setTimeout
    js/window
    (fn []
      (testing "Then the app is started"

        ;; (println (.-outerHTML (aget (.-children js/document) 0)))
        (is (some? (.getElementById js/document "loading")))

        (is (= :started @main/state)))
      (is (some? (.getElementById js/document "done")))
      (done))
    1000)))
