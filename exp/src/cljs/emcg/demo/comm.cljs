(ns emcg.demo.comm
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [clojure.string :as s]
   [cljs-http.client :as http]
   [cljs.core.async :refer [chan <! >!]]
   ))

(def ^{:private true} base-url "/demo")

(defn fetch-text []
  (let [res-chan (chan)]
    (go (let [response (<! (http/get (s/join [base-url "/hello"])))]
          (if (:success response)
            (let [resp-text (:body response)]
              (>! res-chan
                  {:msg
                   {:text resp-text}
                   }))
            )))
    res-chan))

(defn fetch-vid []
  (let [res-chan (chan)]
    (go (let [response (<! (http/get (s/join [base-url "/vid"])
                                     {:response-type :blob}))]
          (if (:success response)
            (let [resp-blob (:body response)
                  resp-blob-url (js/URL.createObjectURL resp-blob)]
              (>! res-chan
                  {:vid
                   {:url resp-blob-url}
                   }))
            )))
    res-chan))

(defn fetch-img []
  (let [res-chan (chan)]
    (go (let [response (<! (http/get (s/join [base-url "/pic"])
                                     {:response-type :blob}))]
          (if (:success response)
            (let [resp-blob (:body response)
                  resp-blob-url (js/URL.createObjectURL resp-blob)]
              (>! res-chan
                  {:img
                   {:url resp-blob-url}
                   }))
            )))
    res-chan))

(defn fetch-thingones []
  (let [res-chan (chan)]
    (go (let [response (<! (http/get (s/join [base-url "/thingone"])))]
          (if (:success response)
            (>! res-chan (:body response))
            )))
    res-chan))

(defn create-thingone [name]
  (let [res-chan (chan)]
    (go (let [response
              (<! (http/post
                   (s/join [base-url "/thingone"])
                   {:edn-params {:name name}}))]
          (>! res-chan
              (:body response))))
    res-chan))
