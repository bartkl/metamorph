(ns schema-transformer.utils
  (:import (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.model.util Values)))

(defn map-values [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn get-static-field-value [cls name]
  (as-> (.getDeclaredFields cls) v
    (filter #(= (.getName %) name) v)
    (first v)
    (.get v (.getType v))))

(defn iri-from-filename [path]
  (Values/iri (str "http://" path)))