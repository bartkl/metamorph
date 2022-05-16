(ns schema-transformer.asami
  (:require [asami.core :as d]
            [clojure.java.io :as io]
            [schema-transformer.utils :as utils]
            [schema-transformer.rdf :as rdf])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.model.vocabulary RDF RDFS SHACL)
           (org.eclipse.rdf4j.rio RDFFormat)
           (org.eclipse.rdf4j.model.util Values)))



(def db-uri "asami:mem://profile")
(d/create-database db-uri)
(d/delete-database db-uri)

(def conn (d/connect db-uri))

;; (def model
;;   (let [m (rdf/read-directory (io/file "resources/example-profile/")
;;                               (fn [path] (into-array IRI [(utils/iri-from-filename path)])))]
;;     (map #(assoc {} :db/ident (:rdf/subject %) (:rdf/predicate %) (:rdf/object %)) m)))


(def model
   (rdf/read-directory (io/file "resources/example-profile/")))

(take 2 model)

@(d/transact conn {:tx-triples model})

(d/q '[:find ?s ?p ?o :where [?s ?p ?o]] conn)

;; (.toString RDF/TYPE)  ;; Does not work because `q` is a macro I guess?

(as->
 (d/q '[:find [?r ...] ;;?label ?comment
        :where
        [?r "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" "http://www.w3.org/ns/shacl#NodeShape"]
        [?r "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" "http://www.w3.org/ns/shacl#NodeShape"]

        ;; [?r "http://www.w3.org/2000/01/rdf-schema#comment" "\"RootObject\"^^<http://www.w3.org/2001/XMLSchema#string>"]
        [?r "http://www.w3.org/ns/shacl#targetClass" ?targetClass]
        [?r "http://www.w3.org/ns/shacl#property" * ?p]
        ;; [?targetClass "http://www.w3.org/2000/01/rdf-schema#comment" ?comment]
        ;; [?targetClass "http://www.w3.org/2000/01/rdf-schema#label" ?label]
        ]conn) v
  (map #(d/entity conn % true) v) (first v))
      ;; (first v)
;;  (d/entity conn v true))

(d/entity (d/db conn) (java.net.URI. "https://w3id.org/schematransform/ExampleShape#EShape") true)
(d/entity (d/db conn) "https://w3id.org/schematransform/ExampleShape#BShape" true)
(d/entity (d/db conn) "_:e0047c979d3f4f7ebe4d980b2fd52b7293" true)
(d/entity (d/db conn) "https://w3id.org/schematransform/ExampleShape#idShape" true)

;; (->>
;; (map #(d/entity conn %) (d/q '[:find [?x ...] :where [?x ?y "https://w3id.org/schematransform/ExampleVocabulary#D"]] conn))
;; (take 1)
;;  )
