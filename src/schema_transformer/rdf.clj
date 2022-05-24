(ns schema-transformer.rdf
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [ont-app.vocabulary.core :as vocab]
            [clojure.spec.alpha :as s]
            [schema-transformer.utils :as utils])
  (:import (org.eclipse.rdf4j.rio Rio)
           (org.eclipse.rdf4j.model IRI)
           (org.eclipse.rdf4j.rio RDFFormat)
           (org.eclipse.rdf4j.model.util Values)))


(def supported-files #{"ttl", "rdf", "jsonld"})

(defn- simple-statement->triple
  "Creates a triple from an rdf4j `SimpleStatement.`"

  [st]

  (let [subject (.getSubject st)
        predicate (.getPredicate st)
        object (.getObject st)]
    [(if (.isIRI subject)
       (vocab/keyword-for (str subject))
       (keyword (str subject)))

     (vocab/keyword-for (str predicate))

     (cond
       (.isLiteral object) (str object)
       (.isIRI object) (vocab/keyword-for (str object))
       :else (keyword (str object)))]))


(defn read-file
  "Reads RDF file."

  [path]
  {:pre [(.isFile path)]}

  ;; TODO: Make context function optional. Using `let`?
  (with-open [rdr (clojure.java.io/reader path)]
    (into (hash-set)
          (map simple-statement->triple
               (Rio/parse rdr RDFFormat/TURTLE (into-array IRI []))))))

(defn read-directory
  "Reads all RDF files found in `path` and returns a set of all
   statements."

  [path]
  {:pre [(.isDirectory path)]}

  (->> (file-seq path)
       (filter #(supported-files (utils/file-ext %)))
       (map read-file)
       (reduce set/union)))

(read-directory (io/file "resources/example-profile/"))

;; TODO: Improve this call... See below for a start, although that too seems sub-optimal.
(read-file (io/file "resources/example-profile/Profile.ttl"))

;; (utils/apply-fns-to-arg [#(utils/iri-from-filename %)
;;                          #(Values/iri "http://some-other-iri")]
;;                         (io/file "resources/example-profile/Profile.ttl"))

;; (utils/apply-fns-to-arg [+ *] 1 2)

(vocab/put-ns-meta!
 'ont-app.vocabulary.prof
 {:vann/preferredNamespacePrefix "prof"
  :vann/preferredNamespaceUri "http://www.w3.org/ns/dx/prof/"})