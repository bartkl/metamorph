; SPDX-FileCopyrightText: 2023 Bart Kleijngeld
;
; SPDX-License-Identifier: Apache-2.0

(ns metamorph.rdf.reading
  (:require [clojure.java.io :as jio]
            [clojure.set :as set]
            [ont-app.vocabulary.core :as vocab]
            [metamorph.rdf.datatype :as datatype]
            [metamorph.utils.file :as utils.file])
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

(defmulti read-triples
  "Reads triples from RDF files."

  {:arglists '([path])}
  utils.file/type :default :invalid-type)

(defmethod read-triples :directory [path]
  (->> (file-seq path)
       (filter file-supported?)
       (map read-triples)
       (reduce set/union)))

(defmethod read-triples :file [path]
  (with-open [rdr (jio/reader path)]
    (let [ctxs (into-array IRI [])]
      (->> (Rio/parse rdr RDFFormat/TURTLE ctxs)
           (map simple-statement->triple)))))

(defmethod read-triples :invalid-type [_] "Please provide a path to a directory or file to read.")
