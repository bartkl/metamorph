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

(defn count->int [s]
  (-> (string/split s #"\^\^")
      (first)
      (string/replace #"\"" "")
      Integer/parseInt))


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

;;;;;;;;;;;;;;;;; AVRO.

(defn iri-local-name [kw]
  (as-> (str kw) v
    (subs v 1)
    (URI. v)
    (.getFragment v)
    (keyword v)))

(defn record-name [node]
  (iri-local-name
   (get-in node [:sh/targetClass :id :id])))

(defn enum-symbol [node]
  (iri-local-name
   (get-in node [:id :id])))

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

(defn field-doc [node]
  ;; ""
  (get-in node [:sh/path :rdfs/comment] ""))


(defn- field-schema [node type]
  (let [min-count (count->int (:sh/minCount node "0"))
        max-count (count->int (:sh/maxCount node "99"))]
    ((cardinality->schema-fn [(min min-count 1)
                              (if (> max-count 1) :* max-count)])
     type)))

;; (def g (get-properties root-node))
;; (def g-props (get-properties node))

;; (def node (nth g 2))
;; (get-in node [:sh/path :id])
;; (node :sh/node)

;; (avro-schema (node :sh/node))

(defn rdf-list->seq [rdf-list]
  (if (nil? rdf-list) '() ;; TODO: Improve.
      (loop [l rdf-list
             s (list)]
        (if (= l :rdf/nil)
          s
          (recur (l :rdf/rest) (conj s (l :rdf/first)))))))

(declare get-properties)

(defn get-inherited-props [shape]
  (let [other-shapes (rdf-list->seq (shape :sh/and))]
    (if (not (empty? other-shapes))
      (mapcat get-properties other-shapes)
      '())))

(defn- properties [shape]
  (let [p (:sh/property shape)]
    (->>
     (if (map? p) (list p) p)
     (filter #(not= (keys (% :sh/node)) '(:id))))))

(defn get-properties [node]
  (into #{} (concat
             (properties node)
             (get-inherited-props node))))

(declare avro-field)

(defn avro-schema [root-node]
  (if (contains? root-node :sh/in)
    (l/enum-schema
     (record-name root-node)
     (record-doc root-node)
     (map enum-symbol (rdf-list->seq (:sh/in root-node))))
    (let [properties (get-properties root-node)]
      (l/record-schema
       (record-name root-node)
       (record-doc root-node)
       (if (some? properties)
         (map avro-field properties)
         (vector))))))

(defn avro-field [node]
  (let [type (condp #(get %2 %1) node
               :sh/datatype :>> datatype-sh->avro
               :sh/node :>> #(when (not= (keys %) '(:id)) (avro-schema %))
               nil)]
    [(field-name node)
     (field-doc node)
     :required   ;; Hack required to disable optionality. Maybe schemes and such do work though.
     (field-schema node type)]))

;; (l/default-data B)
;; (l/edn B)
(comment
  (l/edn (l/record-schema :name :doc [[:a "d" :required l/string-schema]]))


  (def db-uri "asami:mem://profile")
  (d/create-database db-uri)
  (d/delete-database db-uri)

  (def conn (d/connect db-uri))

  (def model
    (rdf/read-directory (io/file "resources/example_profile/")))

  (take 2 model)

  @(d/transact conn {:tx-triples model})

  (mark-resources-as-entities conn)



;;  (def start-node (d/entity conn :a//w3id.org/schematransform/ExampleShape#BShape true))
;;  (def a-shape (d/entity conn :https://w3id.org/schematransform/ExampleShape#AShape true))
;;  (def d-shape (d/entity conn :https://w3id.org/schematransform/ExampleShape#DShape true))
;;  (def root-node start-node)
;;  (map #(get-in % [:sh/path]) (get-inherited-props a-shape))

;;  (def a (avro-schema start-node))
;;  (l/edn a)

;;  (spit "testBShape.json" (l/json a))
  )