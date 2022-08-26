; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.schemas.avro.schema
  (:require [deercreeklabs.lancaster :as l]
            [metamorph.graph.db :as graph.db]
            [metamorph.graph.shacl :as shacl]
            [metamorph.rdf.datatype :refer [rdf-list->seq]]
            [metamorph.schemas.avro.cardinality :refer [cardinality->schema-fn]]
            [metamorph.schemas.avro.datatype :refer [xsd->avro]]
            [metamorph.utils.uri :as utils.uri]))

(declare property->record-field
         avro-schema)

;; Enum
(defn- enum-name [n]
  (utils.uri/fragment
   (get-in n [:sh/targetClass :id :id])))

(defn- enum-doc [n]
  (let [target-class (get n :sh/targetClass)]
    (target-class :rdfs/comment)))

(defn- enum-symbol [node]
  (utils.uri/fragment
   (get-in node [:id :id])))

(defn- node-shape->enum [n]
  (l/enum-schema
   (enum-name n)
   (enum-doc n)
   (map enum-symbol (rdf-list->seq (:sh/in n)))))

;; Record
(defn- record-name [n]
  (utils.uri/fragment
   (get-in n [:sh/targetClass :id :id])))

(defn- record-doc [n]
  (get-in n [:sh/targetClass :rdfs/comment]))

(defn- node-shape->record [n]
  (let [properties (remove shacl/property-node-ref? (shacl/properties n))]
    (l/record-schema
     (record-name n)
     (record-doc n)
     (if (some? properties)
       (map property->record-field properties)
       (vector)))))

;; Record field
(defn- record-field-name [p]
  (utils.uri/fragment (get-in p [:sh/path :id :id])))

(defn- record-field-doc [p]
  (get-in p [:sh/path :rdfs/comment] ""))

(defn- record-field-schema [p type]
  (let [min-count (:sh/minCount p 0)
        max-count (:sh/maxCount p ##Inf)]
    ((cardinality->schema-fn [(min min-count 1)
                              (if (> max-count 1) :* max-count)])
     type)))

(defn- property->record-field [p]
  (let [type (condp #(get %2 %1) p  ;; TODO: Improve
               :sh/datatype :>> xsd->avro
               :sh/node :>> avro-schema
               nil)]
    [(record-field-name p)
     (record-field-doc p)
     :required   ;; Hack required to disable optionality. Maybe schemes and such do work though.
     (record-field-schema p type)]))

;; Schema build entry point
(defn avro-schema [n]
  (if (contains? n :sh/in)
    (node-shape->enum n)
    (node-shape->record n)))
