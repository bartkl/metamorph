(ns schema-transformer.asami
  (:require [asami.core :as d]
            [deercreeklabs.lancaster :as l]
            [clojure.java.io :as io]
            [schema-transformer.utils :as utils]
            [schema-transformer.rdf :as rdf])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (java.net URI)
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



@(d/transact conn {:tx-triples [
  [:https://w3id.org/schematransform/ExampleShape#BShape :id :https://w3id.org/schematransform/ExampleShape#BShape]
  [:https://w3id.org/schematransform/ExampleShape#DShape :id :https://w3id.org/schematransform/ExampleShape#DShape]
  [:https://w3id.org/schematransform/ExampleShape#BShape :a/entity true]
  [:https://w3id.org/schematransform/ExampleShape#DShape :a/entity true]]})

(defn bnode? [kw]
     (clojure.string/starts-with? (str kw) ":_:"))

(defn get-resources [conn]
    (map first 
        (d/q '[:find ?e
            :in $ ?is-bnode
            :where [?e _ _]
                    (not [(?is-bnode ?e)])]
            conn bnode?)))

(defn mark-resource-as-entity [r] (vector r :a/entity true))
(defn add-id [r] (vector r :id r))

(mapcat #(list
          (mark-resource-as-entity %)
          (add-id %))
        (get-resources conn))


(d/q '[:find ?s ?p ?o :where [?s ?p ?o]] conn)
;; (d/q '[:find ?t :where [:https://w3id.org/schematransform/ExampleShape#AShape :sh/targetClass ?t]] conn)

;; (.toString RDF/TYPE)  ;; Does not work because `q` is a macro I guess?

;; (as->gg
;;  (d/q '[:find [?r ...] ;;?label ?comment
;;         :where
;;         [?r "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" "http://www.w3.org/ns/shacl#NodeShape"]
;;         [?r "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" "http://www.w3.org/ns/shacl#NodeShape"]

;;         ;; [?r "http://www.w3.org/2000/01/rdf-schema#comment" "\"RootObject\"^^<http://www.w3.org/2001/XMLSchema#string>"]
;;         [?r "http://www.w3.org/ns/shacl#targetClass" ?targetClass]
;;         [?r "http://www.w3.org/ns/shacl#property" * ?p]
;;         ;; [?targetClass "http://www.w3.org/2000/01/rdf-schema#comment" ?commen
;;         ;; [?targetClass "http://www.w3.org/2000/01/rdf-schema#label" ?label]
;;         ]conn) v
;;   (map #(d/entity conn % true) v) (first v))
      ;; (first v)
;;  (d/entity conn v true))

(d/entity (d/db conn) :https://w3id.org/schematransform/ExampleShape#BShape true)
;; (def a-shape (d/entity (d/db conn) :https://w3id.org/schematransform/ExampleShape#AShape false))
(def start-node (d/entity (d/db conn) :https://w3id.org/schematransform/ExampleShape#EShape false))
(d/entity (d/db conn) (java.net.URI. "https://w3id.org/schematransform/ExampleShape#idShape") true)


(c-shape :id)

;; (->>
;; (map #(d/entity conn %) (d/q '[:find [?x ...] :where [?x ?y "https://w3id.org/schematransform/ExampleVocabulary#D"]] conn))
;; (take 1)
;;  )

(defn target-class-iri [node conn]
  (d/q '[:find ?t .
         :in $ ?node
         :where [?node :sh/targetClass ?t]]
       conn node))

(defn record-name [iri-kw]
  (as-> (str iri-kw) v
    (subs v 1)
    (URI. v)
    (.getFragment v)
    (keyword v)))

(defn record-doc [node] (get-in node [:sh/targetClass :rdfs/comment]))
(defn avro-field [node]
    (l/string-schema "Hallo"))

(defn avro-schema [node name conn]
  (l/record-schema
   (record-name (target-class-iri name conn))
   (record-doc node)
   (map avro-field (:sh/property node))))

;; (l/default-data B)
;; (l/edn B)

(def start-node (d/entity (d/db conn) :https://w3id.org/schematransform/ExampleShape#EShape false))
(def node start-node)

(def a (avro-schema start-node :https://w3id.org/schematransform/ExampleShape#AShape conn))
(l/edn a)

;; (record-name (target-class-iri :https://w3id.org/schematransform/ExampleShape#CShape conn))