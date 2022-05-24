(ns schema-transformer.rdf
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [ont-app.vocabulary.core :as vocab]
            [schema-transformer.utils.file :as utils.file])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.model.util Values)
           (org.eclipse.rdf4j.model.base CoreDatatype$XSD CoreDatatype$RDF)
           (org.eclipse.rdf4j.rio RDFFormat)))

(def supported-file-exts #{"ttl", "rdf", "jsonld"})

(defn literal->type [x]
  (condp #(= (.getCoreDatatype %2) %1) x
    CoreDatatype$XSD/STRING (.stringValue x)
    CoreDatatype$XSD/BOOLEAN (.booleanValue x)
    CoreDatatype$XSD/INTEGER (.intValue x)
    CoreDatatype$XSD/LONG (.integerValue x)
    CoreDatatype$XSD/DECIMAL (.decimalValue x)
    CoreDatatype$XSD/FLOAT (.floatValue x)
    CoreDatatype$XSD/DOUBLE (.doubleValue x)
    CoreDatatype$XSD/DURATION (.stringValue x)
    CoreDatatype$XSD/DATETIME (.stringValue x)
    CoreDatatype$XSD/TIME (.stringValue x)
    CoreDatatype$XSD/DATE (.stringValue x)
    CoreDatatype$XSD/ANYURI (.stringValue x)
    CoreDatatype$RDF/LANGSTRING (str x)))

(defn- simple-statement->triple
  "Creates a triple from an RDF4j `SimpleStatement.`"
  [st]

  (let [subject (.getSubject st)
        predicate (.getPredicate st)
        object (.getObject st)]
    [(if (.isIRI subject)
       (vocab/keyword-for (str subject))
       (keyword (str subject)))

     (vocab/keyword-for (str predicate))

     (cond
       (.isLiteral object) (literal->type object)
       (.isIRI object) (vocab/keyword-for (str object))
       :else (vocab/keyword-for (str object)))]))  ;;  Blank node

(defn read-file
  "Reads RDF file."
  [path]

  {:pre [(.isFile path)]}

  (with-open [rdr (clojure.java.io/reader path)]
    (let [ctxs (into-array IRI [])]
      (into (hash-set)
            (map simple-statement->triple
                 (Rio/parse rdr RDFFormat/TURTLE ctxs))))))

(defn read-directory
  "Reads all RDF files found in `path` and returns a set of all
   statements."
  [path]

  {:pre [(.isDirectory path)]}

  (->> (file-seq path)
       (filter #(supported-file-exts (utils.file/ext %)))
       (map read-file)
       (reduce set/union)))