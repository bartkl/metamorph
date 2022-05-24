(ns schema-transformer.schemas.avro.schema
  (:require [deercreeklabs.lancaster :as l]
            [schema-transformer.schemas.avro.cardinality :refer [cardinality->schema-fn]]
            [schema-transformer.schemas.avro.datatype :refer [xsd->avro]]
            [schema-transformer.graph.shacl :as shacl]
            [schema-transformer.utils.datatype :refer [count->int rdf-list->seq]]
            [schema-transformer.utils.uri :as utils.uri]))

(declare property->record-field)
(declare avro-schema)

;; Enum
(defn- enum-name [shape]
  (utils.uri/iri-local-name
   (get-in shape [:sh/targetClass :id :id])))

(defn- enum-doc [shape]
  (let [target-class (get shape :sh/targetClass)]
    (target-class :rdfs/comment)))

(defn- enum-symbol [node]
  (utils.uri/iri-local-name
   (get-in node [:id :id])))

(defn- shape->enum [shape]
  (l/enum-schema
   (enum-name shape)
   (enum-doc shape)
   (map enum-symbol (rdf-list->seq (:sh/in shape)))))

;; Record
(defn- record-name [shape]
  (utils.uri/iri-local-name
   (get-in shape [:sh/targetClass :id :id])))

(defn- record-doc [shape]
  (let [target-class (get shape :sh/targetClass)]
    (target-class :rdfs/comment)))

(defn- shape->record [shape]
  (let [properties (shacl/properties shape)]
    (l/record-schema
     (record-name shape)
     (record-doc shape)
     (if (some? properties)
       (map property->record-field properties)
       (vector)))))

;; Record field
(defn- record-field-name [property]
  (utils.uri/iri-local-name (get-in property [:sh/path :id :id])))

(defn- record-field-doc [property]
  (get-in property [:sh/path :rdfs/comment] ""))

(defn- record-field-schema [node type]
  (let [min-count (count->int (:sh/minCount node "0"))
        max-count (count->int (:sh/maxCount node "99"))]
    ((cardinality->schema-fn [(min min-count 1)
                              (if (> max-count 1) :* max-count)])
     type)))

(defn- property->record-field [prop]
  (let [type (condp #(get %2 %1) prop  ;; TODO: Improve
               :sh/datatype :>> xsd->avro
               :sh/node :>> #(when (not= (keys %) '(:id)) (avro-schema %))
               nil)]
    [(record-field-name prop)
     (record-field-doc prop)
     :required   ;; Hack required to disable optionality. Maybe schemes and such do work though.
     (record-field-schema prop type)]))

;; Schema build entry point
(defn avro-schema [shape]
  (if (contains? shape :sh/in)
    (shape->enum shape)
    (shape->record shape)))