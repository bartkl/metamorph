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

(defn file-supported? [path]
  (let [supported-exts #{"ttl"}]
    (contains? supported-exts (utils.file/ext path))))

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

(defn read-triples-from-file
  [path]
  (with-open [rdr (jio/reader path)]
    (let [ctxs (into-array IRI [])]
      (->> (Rio/parse rdr RDFFormat/TURTLE ctxs)
           (map simple-statement->triple)))))

(defn read-triples-from-directory
  [path]
  (->> (file-seq path)
       (filter file-supported?)
       (map read-triples-from-directory)
       (reduce set/union)))

(defmulti read-triples
  "Reads triples from RDF files."

  {:arglists '([path])}
  utils.file/type :default :invalid-type)

(defmethod read-triples :directory [path] (read-triples-from-directory path))
(defmethod read-triples :file [path] (read-triples-from-file path))
(defmethod read-triples :invalid-type [_] "je moeder")