(ns schema-transformer.schemas.avro.enum
  (:require [schema-transformer.utils.uri :as utils.uri]
            [deercreeklabs.lancaster :as l]
            [schema-transformer.utils.datatype :refer [rdf-list->seq]]))

(defn- enum-name [shape]
  (utils.uri/iri-local-name
   (get-in shape [:sh/targetClass :id :id])))

(defn- enum-doc [shape]
  (let [target-class (get shape :sh/targetClass)]
    (target-class :rdfs/comment)))

(defn- enum-symbol [node]
  (utils.uri/iri-local-name
   (get-in node [:id :id])))

(defn shape->enum [shape]
  (l/enum-schema
   (enum-name shape)
   (enum-doc shape)
   (map enum-symbol (rdf-list->seq (:sh/in shape)))))