(ns schema-transformer.utils.datatype
  (:require [clojure.string :as string]))

(defn rdf-list->seq [rdf-list]
  (if (nil? rdf-list) '() ;; TODO: Improve.
      (loop [l rdf-list
             s (list)]
        (if (= l :rdf/nil)
          s
          (recur (l :rdf/rest) (conj s (l :rdf/first)))))))

(defn count->int [s]
  (-> (string/split s #"\^\^")
      (first)
      (string/replace #"\"" "")
      Integer/parseInt))