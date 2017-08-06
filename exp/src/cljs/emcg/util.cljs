(ns emcg.util)

(defn count-map-lists [map-of-lists]
  (reduce
   (fn [acc [key val]]
     (assoc acc key (count val))) {}
   map-of-lists))

