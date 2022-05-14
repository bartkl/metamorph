(ns schema-transformer.lancaster-poc
  (:require [deercreeklabs.lancaster :as l]
            [schema-transformer.rdf :as rdf]
            [clojure.java.io :as io]
            [schema-transformer.utils :as utils])
  (:import (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.model.util Values)))

(l/def-record-schema D
  "This is yet another class"
  [:id :required l/string-schema]
  [:def l/int-schema])

(l/def-record-schema B
  "This is a sub-class"
  [:fromBtoD "Association from B to D" :required D]
  [:fromBtoDButSomehowDifferent "Association from B to D but somehow different" :required (l/map-schema D)])


;; (with-open [w (io/writer "testme.json")]
;;   (.write w (l/json B)))

(l/default-data B)
(l/edn B)


;; From RDF model [WIP]

(def example-model
  (rdf/read-file (io/file "resources/example-profile/Constraints.ttl")
                 (fn [path] (into-array IRI [(utils/iri-from-filename path)
                                             (Values/iri "http://some-other-iri")]))))

(filter #(and (= (:rdf/predicate %) "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
              (= (:rdf/object %) "http://www.w3.org/ns/shacl#NodeShape"))
        example-model)