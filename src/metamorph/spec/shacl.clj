; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.spec.shacl
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.set :as set]
            [metamorph.graph.db :refer [node-ref?]]
            [metamorph.spec.xsd]
            [metamorph.spec.owl])
  (:import (java.net URI URISyntaxException)))

;; (defn specs-for-ns [ns']
;;   (into {}
;;         (filter #(= (namespace (first %)) ns')
;;                 (s/registry))))

(defn one-key-of [kws]
  (fn [m]
    (= (count (set/intersection (into #{} kws)
                                (into #{} (keys m))))
       1)))

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
   (one-key-of [:sh/node :sh/datatype])
   #(if (every? some? [:sh/minCount :sh/maxCount])
      (<= (:sh/minCount %) (:sh/maxCount %))
      true)))


(s/def :sh/nodeShape
  (s/keys :req [:sh/targetClass]
          :opt [:sh/property
                :rdfs/comment
                :rdfs/label
                :sh/in
                :sh/and]))