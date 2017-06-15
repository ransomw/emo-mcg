(ns emcg.state
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require
   [cljs.core.async :refer [chan <!]]
   ))

;; ;; todo: duplicates server-side 'emcg.expone variable.
;; ;;  move to common/
;; (def exp-stim-config
;;   {:num-emo (count emo-stim-filenames)
;;    :num-mcg (count mcg-stim-filenames)})

(defonce app-state
  (atom
   {:init-err nil
    :exp-def nil
    :stim-infos {:emo (list) :mcg (list)}
    :exp-res {:emo (list) :mcg (list)}
    }))

(defonce update-chan (chan))

(defn count-stims
  "total number of stimuli, both emo and mcg, for a given experiment"
  [exp-def]
  (let [mcg-blocks (or (:defn exp-def) exp-def)]
    (+ (count mcg-blocks)
       (reduce + (map (comp count :mcg-ids) mcg-blocks)))))


(defn ^{private true}
  make-id-list-updater [id-list-lookup]
  (fn [data]
    (if (not (contains?
              (set (map :id (get-in (deref app-state)
                                    id-list-lookup)))
              (:id data)))
      (swap! app-state update-in id-list-lookup
             #(conj % data)))))

(def ^{private true}
  update-listeners
  {:exp-def
   (fn [data]
     (if (not (:exp-def (deref app-state)))
       (swap! app-state assoc :exp-def data)))
   :emo-stim (make-id-list-updater [:stim-infos :emo])
   :mcg-stim (make-id-list-updater [:stim-infos :mcg])
   :emo-res (make-id-list-updater [:exp-res :emo])
   :mcg-res (make-id-list-updater [:exp-res :mcg])
   })

(defn ^{private true}
  handle-update-info [update-info]
  (let [update-keys (keys update-info)]
    (map
     (fn [[update-listener update-data update-key]]
       (if update-listener
         (update-listener update-data)))
     (map list
          (map (partial get update-listeners) update-keys)
          (map (partial get update-info) update-keys)
          update-keys)
     )))

(defn ^{private true}
  listen-for-updates []
  (go
    (while true
      (let [update-info (<! update-chan)]
        (if (contains?
             (doall
              (handle-update-info update-info))
             nil)
          (do
            (print "some error handling the following update info")
            (print update-info)))
        ))))

(listen-for-updates)
