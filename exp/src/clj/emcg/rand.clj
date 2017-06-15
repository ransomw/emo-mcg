(ns emcg.rand
  )


(defn rand-permute-helper [list-to-permute permuted-list]
  (if (= list-to-permute '())
    permuted-list
    ;; `count` is clojure's `length`
    (let [rand-idx (rand-int (count list-to-permute))
          rand-slice-head (take rand-idx list-to-permute)
          rand-slice-tail (drop rand-idx list-to-permute)]
      (rand-permute-helper
       ;; `~` removes clojure namespace
       (flatten `(~,rand-slice-head ~,(drop 1 rand-slice-tail)))
       (cons (first rand-slice-tail) permuted-list)))
    ))


(defn rand-permute [list-to-permute]
  (rand-permute-helper list-to-permute '()))


;; sort of like a "multiset"
(defn rand-multilist [n avail-els]
  (take ; handle rounding error
   n (flatten (repeatedly
               (/ n (count avail-els))
               (fn [] (rand-permute avail-els))))))


