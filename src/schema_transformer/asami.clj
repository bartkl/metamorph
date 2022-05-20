(ns schema-transformer.asami
  (:require [asami.core :as d]
            [deercreeklabs.lancaster :as l]
            [clojure.java.io :as io]
            [clojure.string :as string]
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
;; (d/delete-database db-uri)

(def conn (d/connect db-uri))

(def model
  (rdf/read-directory (io/file "resources/example-profile/")))

(take 2 model)

@(d/transact conn {:tx-triples model})

(defn count->int [s]
(-> (string/split s #"\^\^")
    (first)
    (string/replace #"\"" "")
    Integer/parseInt)
  )

(defn bnode? [kw]
     (string/starts-with? (str kw) ":_:"))

(defn get-resources [conn]
    (map first
         (d/q '[:find ?e
                ;; :in $ ?is-bnode
                :where [?e _ _]]
              conn)))
                ;; (not [(?is-bnode ?e)])]
              ;; conn bnode?)))

(defn mark-resource-as-entity [r] (vector r :a/entity true))
(defn add-id [r] (vector r :id r))

(defn mark-resources-as-entities [conn]
  @(d/transact conn {:tx-triples
                     (mapcat #(list
                               (mark-resource-as-entity %)
                               (add-id %))
                             (get-resources conn))}))

(mark-resources-as-entities conn)

;; (d/q '[:find ?s ?p ?o :where [?s ?p ?o]] conn)
;;;;;;;;;;;;;;;;; AVRO.

(defn iri-local-name [kw]
  (println kw)
  (as-> (str kw) v
    (subs v 1)
    (URI. v)
    (.getFragment v)
    (keyword v)))

(defn record-name [node]
  (iri-local-name
   (get-in node [:sh/targetClass :id :id])))

(defn record-doc [node]
  (let [target-class (get node :sh/targetClass)]
    (target-class :rdfs/comment)))

(def datatype-sh->avro
  #:xsd{:string l/string-schema
        :double l/double-schema
        :boolean l/boolean-schema
        :int l/int-schema
        :decimal l/bytes-schema
        :float l/float-schema
        :duration l/fixed-schema
        :dateTime l/string-schema
        :date l/string-schema
        :time l/string-schema
        :anyURI l/string-schema})

(def cardinality->schema-fn
  {[1  1] identity
   [0  1] l/maybe
   [1 :*] l/array-schema
   [0 :*] (comp l/maybe l/array-schema)})

(defn- field-name [node]
  (iri-local-name (get-in node [:sh/path :id :id])))

(defn- field-schema [node type]
  (let [min-count (count->int (node :sh/minCount))
        max-count (count->int (node :sh/maxCount))]
    ((cardinality->schema-fn [(min min-count 1)
                              (if (> max-count 1) :* max-count)])
     type)))

(def g (get-properties root-node))
(def g-props (get-properties node))

(def node (nth g 2))
(get-in node [:sh/path :id])
(node :sh/node)

(avro-schema (node :sh/node))

(defn avro-field [node]
  (let [type (condp #(get %2 %1) node
               :sh/datatype :>> datatype-sh->avro
               :sh/node :>> #(when (not= (keys %) '(:id)) (avro-schema %))
               nil)]
    (if (some? type)
      [(field-name node)
       :required   ;; Hack required to disable optionality. Maybe schemes and such do work though.
       (field-schema node type)]
      [])))

(defn rdf-list->seq [rdf-list]
  (loop [l rdf-list
         s (list)]
    (if (= l :rdf/nil)
      s
      (recur (l :rdf/rest) (conj s (l :rdf/first))))))

(defn get-inherited-props [shape]
  ())
  ;; (loop [other-shapes shape
  ;;        props (list)]
  ;;   (rdf-list->seq (shape :sh/and))))


(defn get-properties [node]
  (concat
   (:sh/property node)
   (get-inherited-props node)))

(defn avro-schema [root-node]
  (let [properties (get-properties root-node)]
    (l/record-schema
     (record-name root-node)
     (record-doc root-node)
     (if (some? properties)
       (map avro-field properties)
       (vector)))))
   

;; (l/default-data B)
;; (l/edn B)

(def start-node (d/entity conn :https://w3id.org/schematransform/ExampleShape#BShape true))
(def d-shape (d/entity conn :https://w3id.org/schematransform/ExampleShape#DShape true))
(def root-node start-node)

(def a (avro-schema start-node))
(l/edn a)

(l/record-schema :name :doc [["a" "d" l/string-schema]])
