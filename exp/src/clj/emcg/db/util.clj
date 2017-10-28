(ns emcg.db.util)

(defn partition-list-of-maps [list-of-maps partition-key]
  (map ;; return a list
   (fn [[keyed-val maps-with-val-at-key]]
     {partition-key keyed-val
      :maps-with-val-at-key
      (map #(dissoc % partition-key) maps-with-val-at-key)})
   (let [all-keyed-vals
         (->> list-of-maps (map #(get % partition-key))
              (set) (seq))
         get-rows-with-keyed-val
         (fn [a-keyed-val]
           (filter #(= (get % partition-key) a-keyed-val) list-of-maps))
         ]
     (map list
          all-keyed-vals
          (map get-rows-with-keyed-val all-keyed-vals)
          )
     )))

(defn seq-only [some-seq]
  (assert (= 1 (count some-seq)))
  (first some-seq))

;; sort of like a "multiset" in that it may have repeated elements
(defn rand-multilist [n avail-els]
  (take ; handle rounding error
   n (flatten (repeatedly
               (/ n (count avail-els))
               (fn [] (list* (shuffle avail-els)))
               ))))
