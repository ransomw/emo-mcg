(ns emcg.demo.act
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [cljs.core.async :refer [<! >! close!]]
   [emcg.demo.state :refer [update-chan]]
   [emcg.demo.comm :as comm]
   ))

(defn fetch-text []
    (let [res-chan (comm/fetch-text)]
      (go (let [{{resp-text :text} :msg} (<! res-chan)]
            (>! update-chan {:msg resp-text})
            (close! res-chan)))))

(defn fetch-vid []
    (let [res-chan (comm/fetch-vid)]
      (go (let [{{resp-url :url} :vid} (<! res-chan)]
            (>! update-chan {:vid resp-url})
            (close! res-chan)))))

(defn fetch-img []
    (let [res-chan (comm/fetch-img)]
      (go (let [{{resp-url :url} :img} (<! res-chan)]
            (>! update-chan {:img resp-url})
            (close! res-chan)))))

(defn fetch-thingones []
    (let [res-chan (comm/fetch-thingones)]
      (go (let [thingones (<! res-chan)]
            (>! update-chan {:thingones thingones})
            (close! res-chan)))))

(defn create-thingone [name]
  (let [res-chan (comm/create-thingone name)]
    (go (let [{new-id :new-id} (<! res-chan)]
          (if new-id (fetch-thingones))
          (close! res-chan)))))

