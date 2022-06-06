(ns schema-transformer.spec.shacl
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.set :as set]
            [schema-transformer.graph.db :refer [node-ref?]]
            [schema-transformer.spec.xsd]
            [schema-transformer.spec.owl])
  (:import (java.net URI URISyntaxException)))

;; (defn specs-for-ns [ns']
;;   (into {}
;;         (filter #(= (namespace (first %)) ns')
;;                 (s/registry))))

(defn only-one-key-of [kws & req]
  (fn [m]
    (not (set/subset? kws
                      (into #{} (keys m))))))

(s/def :sh/targetClass :owl/Class)
;; (s/def :sh/and (s/* (s/alt :node-shape :sh/nodeShape
;;                            :property-shape :sh/propertyShape)))
(s/def :sh/and :rdf/List)
(s/def :rdfs/comment string?)
(s/def :rdfs/label string?)
(s/def :sh/property (s/coll-of :sh/propertyShape))
;; (s/def :sh/in sequential?)
(s/def :sh/in :rdf/List)
(s/def :sh/path (s/or :obj-prop :owl/ObjectProperty
                      :data-prop :owl/DatatypeProperty))
(s/def :sh/datatype :xsd/datatype)
(s/def :sh/node (s/or :node-shape :sh/nodeShape
                      :node-shape-ref node-ref?))
(s/def :sh/minCount nat-int?)
(s/def :sh/maxCount nat-int?)

(s/def :sh/propertyShape
  (s/and
   (s/keys :req [:sh/path]
           :opt [:sh/node
                 :sh/datatype
                 :sh/minCount
                 :sh/maxCount
                 :sh/label
                 :sh/comment
                 :sh/in])
   (only-one-key-of [:sh/node :sh/datatype])))  ;; TODO: Need to *require* one of both.

(s/def :sh/nodeShape
  (s/keys :req [:sh/targetClass]
          :opt [:sh/property
                :rdfs/comment
                :rdfs/label
                :sh/in
                :sh/and]))