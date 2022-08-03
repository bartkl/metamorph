; SPDX-FileCopyrightText: 2022 Alliander N.V.
;
; SPDX-License-Identifier: Apache-2.0

(ns schema-transformer.rdf.reading
  (:require [clojure.java.io :as jio]
            [clojure.set :as set]
            [ont-app.vocabulary.core :as vocab]
            [schema-transformer.rdf.datatype :as datatype]
            [schema-transformer.utils.file :as utils.file])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.rio RDFFormat)))

(def supported-file-exts #{"ttl", "rdf", "jsonld"})

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
       (.isLiteral object) (datatype/literal->type object)
       (.isIRI object) (vocab/keyword-for (str object))
       :else (vocab/keyword-for (str object)))]))  ;;  Blank node

(defmulti read-triples
  "Reads triples from RDF files."
  {:arglists '([path])}
  utils.file/type :default :invalid-type)

(defmethod read-triples :directory
  [path]
  (->> (file-seq path)
       (filter #(supported-file-exts (utils.file/ext %)))
       (map read-triples)
       (reduce set/union)))

(defmethod read-triples :file
  [path]
  (with-open [rdr (jio/reader path)]
    (let [ctxs (into-array IRI [])]
      (->> (Rio/parse rdr RDFFormat/TURTLE ctxs)
           (map simple-statement->triple)))))

(defmethod read-triples :invalid-type [_] "je moeder")