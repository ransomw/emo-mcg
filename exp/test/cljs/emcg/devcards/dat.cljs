(ns emcg.devcards.dat)

(defonce
  app-state-init
  {:init-err nil
   :exp-def
   {:id 2
    :defn
    (list
     {:emo-id 3 :mcg-ids (list 7 8 9)
      :av-idxs '((2 0) (0 2) (1 1))}
     {:emo-id 4 :mcg-ids (list 10 11 12)
      :av-idxs '((1 1) (0 2) (2 0))})}
   :stim-infos
   {:emo
    (list {:id 4 :url "vid/placeholder_E1.mp4"}
          {:id 3 :url "vid/placeholder_E1.mp4"})
    :mcg
    (list {:id 12 :url "vid/placeholder_V1A1.mp4"}
          {:id 11 :url "vid/placeholder_V1A1.mp4"}
          {:id 8 :url "vid/placeholder_V1A1.mp4"}
          {:id 10 :url "vid/placeholder_V1A1.mp4"}
          {:id 9 :url "vid/placeholder_V1A1.mp4"}
          {:id 7 :url "vid/placeholder_V1A1.mp4"})}
   :exp-res {:emo '() :mcg '()}})

(defn nth-results [results-idx]
  (let [mcg-block (nth (get-in app-state-init [:exp-def :defn])
                       results-idx)
        emo-id (:emo-id mcg-block)
        mcg-ids (:mcg-ids mcg-block)
        av-idx-choices (map first (:av-idxs mcg-block))
        mcg-results
        (map (fn [pair]
               {:id (first pair)
                :click-data {:av-idx (last pair)}})
             (map list mcg-ids av-idx-choices))
        ]
    {:emo-id emo-id :mcg-results mcg-results}
    ))

(let [{first-emo-id :emo-id first-mcg-results :mcg-results}
      (nth-results 0)]
  ;;;;; copied from browser repl <emcg.state>=> @app-state
  ;; :exp-res {:emo ({:id 1}), :mcg ({:id 4, :click-data {:av-idx 2}})}
  ;;;;; after a couple of clicks in the running application
  (defonce
    app-state-one-emo
    ;; (merge-with merge ...) merges one level deep only
    (merge-with
     merge app-state-init
     {:exp-res {:emo (list {:id first-emo-id})}}))
  (defonce
    app-state-one-mcg
    (merge-with
     merge app-state-one-emo
     {:exp-res {:mcg (list (first first-mcg-results))}}))
  (defonce
    app-state-one-block
    (merge-with
     merge app-state-one-mcg
     {:exp-res {:mcg first-mcg-results}}))
  )

(let [{second-emo-id :emo-id second-mcg-results :mcg-results}
      (nth-results 1)]


  (defonce
    app-state-one-block-one-emo
    (update-in
     app-state-one-block [:exp-res :emo]
     ;; use vector to append --- conj prepends to lists
     #(list* (conj (vec %) {:id second-emo-id}))))

  (defonce
    app-state-one-block-one-mcg
    (update-in
     app-state-one-block-one-emo [:exp-res :mcg]
     ;; use vector to append --- conj prepends to lists
     #(list* (conj (vec %) (nth second-mcg-results 0)))))

  (defonce
    app-state-one-block-two-mcg
    (update-in
     app-state-one-block-one-mcg [:exp-res :mcg]
     #(list* (conj (vec %) (nth second-mcg-results 1)))))

  )
