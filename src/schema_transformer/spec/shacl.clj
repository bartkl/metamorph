(ns schema-transformer.spec.shacl
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.set :as set]
            [schema-transformer.spec.xsd])
  (:import (java.net URI URISyntaxException)))

;; (defn specs-for-ns [ns']
;;   (into {}
;;         (filter #(= (namespace (first %)) ns')
;;                 (s/registry))))

(defn iri? [kw]
  (and (keyword? kw)
       (some? (try (uri? (URI. (name kw)))
                   (catch URISyntaxException e)))))

;; (defn only-one [kws]
;;   (fn [m]
;;     (not= (map #(contains? m %) kws)
;;           [true true])))

(defn only-one-key-of [kws]
  (fn [m]
    (not
     (set/subset? kws
                  (into #{} (keys m))))))

(s/def :sh/targetClass iri?)
(s/def :sh/and (s/* iri?))
(s/def :rdfs/comment string?)
(s/def :rdfs/label string?)
(s/def :sh/property (s/* :sh/propertyShape))
(s/def :sh/in (s/cat))
(s/def :sh/path iri?)
(s/def :sh/datatype :xsd/datatype)
(s/def :sh/node iri?)
(s/def :sh/minCount int?)
(s/def :sh/maxCount int?)

(s/def :sh/propertyShape
  (s/and
   (s/keys :req [:sh/path]
           :opt [:sh/node :sh/datatype :sh/minCount :sh/maxCount])
   (only-one-key-of [:sh/node :sh/datatype])))


(s/def :sh/nodeShape
  (s/keys :req [:sh/targetClass]
          :opt [:sh/property :rdfs/comment :rdfs/label :sh/in]))