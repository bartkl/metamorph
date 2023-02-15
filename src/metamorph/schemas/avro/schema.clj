; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.schemas.avro.schema
  (:require [deercreeklabs.lancaster :as l]
    [clojure.string :as string]
    [metamorph.graph.shacl :as shacl]
    [metamorph.rdf.datatype :refer [rdf-list->seq]]
    [metamorph.schemas.avro.cardinality :refer [cardinality->schema-fn]]
    [metamorph.schemas.avro.datatype :refer [xsd->avro]]
    [metamorph.utils.uri :as utils.uri]
    [ont-app.vocabulary.core :as vocab]
    [metamorph.utils.file :as utils.file])
  (:import [java.net URI]))

(declare property->record-field
  avro-schema)

(defn- iri
  ([node t] (vocab/uri-for (get-in node [t :id :id] (node t))))
  ([node] (vocab/uri-for (get-in node [:id :id] node))))

;; Enum
(defn- enum-name [n]
  (utils.uri/name (iri n :sh/targetClass)))

(defn- enum-doc [n]
  (let [target-class (:sh/targetClass n)
        skos-definition (:skos/definition target-class)
        rdfs-comment (:rdfs/comment target-class)]
    (some identity [skos-definition rdfs-comment ""])))

(defn- enum-symbol [node]
  (-> (iri node)
    keyword
    utils.uri/name
    name
    (string/split #"\.")
    last
    keyword))

(defn- node-shape->enum [n]
  (l/enum-schema
    (enum-name n)
    (enum-doc n)
    (map enum-symbol (rdf-list->seq (:sh/in n)))))

;; Record
(defn- record-name [n]
  (utils.uri/name (iri n :sh/targetClass)))

(defn- record-doc [n]
  (let [target-class (:sh/targetClass n)
        skos-definition (:skos/definition target-class)
        rdfs-comment (:rdfs/comment target-class)]
    (some identity [skos-definition rdfs-comment ""])))

(defn- node-shape->record [n]
  (let [properties (->> (shacl/properties n)
                     (remove shacl/property-node-ref?)
                     (sort-by (comp :id :id :sh/path)))]  ;; TODO: Is ordering still unique when there's multiple `nil` coming from this due to lacking properties in the vocabulary?
    ; (println (map #(get-in % [:sh/path :id :id]) properties))
    (l/record-schema
      (record-name n)
      (record-doc n)
      (if (some? properties)
        (map property->record-field properties)
        (vector)))))

;; Record field
(defn- record-field-name [p]
  (-> (iri p :sh/path)
    keyword
    utils.uri/name
    name
    (string/split #"\.")
    last
    keyword))

(defn- record-field-doc [p]
  (let [target-class (:sh/path p)
        skos-definition (:skos/definition target-class)
        rdfs-comment (:rdfs/comment target-class)]
    (some identity [skos-definition rdfs-comment ""])))

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
     :required   ;; Hack required to disable optionality. Maybe schemas and such do work though.
     (record-field-schema p type)]))

;; Schema build entry point
(defn avro-schema [n]
  (if (contains? n :sh/in)
    (node-shape->enum n)
    (node-shape->record n)))

;; Playground.

(comment "Mapping URIs to namespaces"
  (defn iri->schema-name [i]
    (let [uri (URI. i)
          host (string/join "."
                 (-> (.getHost uri)
                   (string/split #"\.")
                   reverse))
          path-parts (-> (.getPath uri)
                       (string/replace-first #"/" "")
                       (string/split #"/"))
          fragment (.getFragment uri)]
      (if (some? fragment)
        (keyword (str host "." (string/join "." path-parts)) fragment)
        (keyword (str host "." (string/join "." (butlast path-parts)) (last path-parts))))))

  (iri->schema-name (vocab/uri-for :https://w3id.org/schematransform/ExampleVocabulary#B))
  (iri->schema-name (vocab/uri-for :https://w3id.org/schematransform/ExampleVocabulary/B))

  (defn- record-name [n]
    (iri->schema-name (iri n :sh/targetClass)))

  (defn- enum-name [n]
    (iri->schema-name (iri n :sh/targetClass))))
