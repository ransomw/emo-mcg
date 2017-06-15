(ns emcg.comm
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [clojure.string :as s]
   [cljs-http.client :as http]
   [cljs.core.async :refer [chan <! >! close!]]
   ))

(def ^{:private true} base-url "/emcg")

;; todo: close chans in go blocks after they're written to
;;  (if no more writes are intended), since closing a channel
;;  prevents writing (puts) to it, not reading from it.
;;
;; then, remove patterns where channels are closed after they're
;; read from

(defn create-exp []
  (let [res-chan (chan)]
    (go (let [response (<! (http/post
                            (s/join [base-url "/exp"])
                            {:edn-params {}}))]
          (if (:success response)
            (>! res-chan {:res (:body response)})
            (>! res-chan {:err (:body response)}))
          ))
    res-chan))

(defn ^{:private true}
  fetch-vid [rel-url]
  (let [res-chan (chan)]
    (go (let [response (<! (http/get (s/join [base-url rel-url])
                                     {:response-type :blob}))]
          (if (:success response)
              (>! res-chan
                  {:res
                   {:url (js/URL.createObjectURL (:body response))}
                   })
              (>! res-chan {:err (:body response)}))))
    res-chan))

(defn ^{:private true}
  fetch-emo-stim [exp-id emo-id]
  (let [res-chan (chan)
        vid-chan (fetch-vid (str "/exp/" exp-id "/emo/" emo-id))]
    (go
      (let [{res-vid :res err-vid :err} (<! vid-chan)]
        (if res-vid
          (>! res-chan {:res {:url (:url res-vid)
                              :emo-id emo-id
                              }})
          (>! res-chan {:err err-vid}))
        (close! vid-chan)
      ))
    res-chan))

(defn ^{:private true}
  fetch-mcg-stim [exp-id emo-id mcg-id]
  (let [res-chan (chan)
        vid-chan (fetch-vid (str "/exp/" exp-id "/emo/"
                                 emo-id "/mcg/" mcg-id))]
    (go
      (let [{res-vid :res err-vid :err} (<! vid-chan)]
        (if res-vid
          (>! res-chan {:res {:url (:url res-vid)
                              :mcg-id mcg-id
                              }})
          (>! res-chan {:err err-vid}))
        (close! vid-chan)
      ))
    res-chan))

(defn fetch-stims [exp-def]
  (let [exp-id (:id exp-def)
        res-chan (chan)]
    (doall
     (->>
      (:defn exp-def)
      (map
       (fn [emo-block]
         (let [emo-id (:emo-id emo-block)
               res-stim-chan (fetch-emo-stim exp-id emo-id)]
           (go (>! res-chan (<! res-stim-chan))
               (close! res-stim-chan))
           (map (partial list emo-id) (:mcg-ids emo-block)))))
      (apply concat)
      (map
       (fn [[emo-id mcg-id]]
         (let [res-stim-chan (fetch-mcg-stim exp-id emo-id mcg-id)]
           (go (>! res-chan (<! res-stim-chan))
               (close! res-stim-chan)))))
      ))
    res-chan))
